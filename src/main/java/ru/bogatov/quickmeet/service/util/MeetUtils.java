package ru.bogatov.quickmeet.service.util;

import org.springframework.data.util.Pair;
import ru.bogatov.quickmeet.config.application.MeetValidationRuleProperties;
import ru.bogatov.quickmeet.entity.BillingAccount;
import ru.bogatov.quickmeet.entity.Guest;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;
import ru.bogatov.quickmeet.model.validation.AccountClassProperties;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MeetUtils {

    private static final Set<String> applicableTransitions = Set.of(
          MeetStatus.PLANNED.getValue() + MeetStatus.ACTIVE.getValue(),
          MeetStatus.PLANNED.getValue() + MeetStatus.CANCELED.getValue(),
          MeetStatus.ACTIVE.getValue() + MeetStatus.FINISHED.getValue()
    );

    public static boolean isStatusEquals(Meet meet, MeetStatus meetStatus) {
        return meet.getMeetStatus().equals(meetStatus);
    }

    public static void checkStatusAndThrow(Meet meet, MeetStatus meetStatus) {
        if (!isStatusEquals(meet, meetStatus)) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Meet state not " + meetStatus.name());
        }
    }

    public static void checkIsGuestApplicable(Meet meet, UUID guestId) {
        if (meet.getOwner().getId().equals(guestId)) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Owner can not be guest");
        }
        if (meet.getUserBlackList() != null && !meet.getUserBlackList().isEmpty() && meet.getUserBlackList().contains(guestId)) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "User in black list");
        }
        if (meet.getGuests().stream().anyMatch(guest -> guest.getUserId().equals(guestId))) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "User already joined");
        }
    }

    public static void checkIsFreeAndThrow(Meet meet) {
        if (meet.getMaxPeople() != 0 && meet.getCurrentPeople() >= meet.getMaxPeople()) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Meet is full");
        }
    }

    public static void addGuest(Meet meet, Guest guest) {
        if (meet.getGuests() == null) {
            meet.setGuests(new HashSet<>());
        }
        meet.getGuests().add(guest);
        meet.setCurrentPeople(meet.getCurrentPeople() + 1);
    }

    public static void validateMeetCreation(MeetCreationBody body) {
        if (body.getTime().isBefore(LocalDateTime.now())) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Meet start date in past");
        }
        if (body.getExpectedDuration() == null || body.getExpectedDuration() > 8 || body.getExpectedDuration() <= 0) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Meet expected duration should be between 1 - 8 hours");
        }
        if (body.getRequiredRank() < 0 || body.getRequiredRank() > 5.0) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Meet required rank should be between 0 - 5");
        }
    }

    public static void validateOwnerClassAndMeetPeriodForCreation(BillingAccount billingAccount, MeetCreationBody body, User owner, Set<Meet> existingMeets, MeetValidationRuleProperties properties, AccountClassProperties accountProperties) {
        if (owner.getAccountRank() < properties.getRequiredRankForMeetCreation()) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, String.format("Owner rank less required %f", properties.getRequiredRankForMeetCreation()));
        }

        int allowedCapacity = accountProperties.getMaxCapacity();

        if (body.getUserAmount() > allowedCapacity && !body.isSkipRules()) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, String.format("Allowed capacity is %d", allowedCapacity));
        }
        if (body.getLocationId() != null) {
            if (billingAccount == null || billingAccount.getBusinessEndTime() == null) {
                throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Business account required");
            } else if (billingAccount.getBusinessEndTime().isBefore(body.getTime())) {
                throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Business account will expire an this time");
            }
        }
        LocalDateTime dayToCreate = body.getTime();
        Set<Meet> todayMeets = existingMeets.stream().filter(meet -> isSameDay(dayToCreate, meet.getDateTime())).filter(meet -> meet.getMeetStatus() != MeetStatus.CANCELED).collect(Collectors.toSet());
        int meetLimit = accountProperties.getLimit();
        validateDayMeetCount(meetLimit, todayMeets);
        if (properties.validateCrossTime) {
            todayMeets = todayMeets.stream().filter(meet -> meet.getMeetStatus() != MeetStatus.FINISHED).collect(Collectors.toSet());
            LocalDateTime endTime = dayToCreate.plusHours(body.getExpectedDuration());
            validateCrossTime(dayToCreate, endTime, todayMeets);
        }
    }

    public static void validateMeetPeriodForUpdate(MeetUpdateBody body, Meet origin, Set<Meet> existingMeets, MeetValidationRuleProperties properties) {
        LocalDateTime dayToCreate = body.getTime() == null ? origin.getDateTime() : body.getTime();
        int expectedDuration = body.getExpectedDuration() == null ? origin.getExpectedDuration() : body.getExpectedDuration();
        Set<Meet> todayMeets = existingMeets.stream()
                .filter(meet -> isSameDay(dayToCreate, meet.getDateTime()))
                .filter(meet -> meet.getMeetStatus() != MeetStatus.CANCELED && !meet.getId().equals(origin.getId()))
                .collect(Collectors.toSet());
        int limit = 1; //= origin.getOwner().getAccountClass() == AccountClass.BASE ? properties.baseLimit : properties.goldLimit;
        validateDayMeetCount(limit, todayMeets);
        if (properties.validateCrossTime) {
            todayMeets = todayMeets.stream().filter(meet -> meet.getMeetStatus() != MeetStatus.FINISHED && meet.getId() != origin.getId() ).collect(Collectors.toSet());
            LocalDateTime endTime = dayToCreate.plusHours(expectedDuration);
            validateCrossTime(dayToCreate, endTime, todayMeets);
        }
    }


    public static boolean isRatingProcessRequired(Meet meet) {
        return !meet.isRatingProcessed();
    }

    public static void validateDayMeetCount(int limit, Set<Meet> todayMeets) {
        if (todayMeets.size() + 1 > limit) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR,
                    String.format("You can create only %d meets, you already created : %s", limit,
                            todayMeets.stream().map(Meet::getName).collect(Collectors.toSet())));
        }
    }

    public static void validateCrossTime(LocalDateTime startTime, LocalDateTime endTime, Set<Meet> todayMeets) {
        todayMeets.forEach(existingMeet -> {
            LocalDateTime existingMeetEnd = existingMeet.getDateTime().plusHours(existingMeet.getExpectedDuration());
            if (existingMeet.getDateTime().isAfter(startTime) && existingMeet.getDateTime().isBefore(endTime)) {
                throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR,
                        String.format("Meet time crosses with another meet start time : %s", existingMeet.getName()));
            }
            if (existingMeetEnd.isAfter(startTime) && existingMeetEnd.isBefore(endTime)) {
                throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR,
                        String.format("Meet time crosses with another meet end time : %s", existingMeet.getName()));
            }
        });
    }

    public static boolean isSameDay(LocalDateTime today, LocalDateTime date) {
        return today.getYear() == date.getYear() && today.getDayOfYear() == date.getDayOfYear();
    }

    public static void validateMeetUpdate(MeetUpdateBody body) {
        if (body.getTime() != null && body.getTime().isBefore(LocalDateTime.now())) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Meet start date in past");
        }
        if (body.getExpectedDuration() != null && ( body.getExpectedDuration() > 8 || body.getExpectedDuration() <= 0)) {
            throw ErrorUtils.buildException(ApplicationError.MEET_VALIDATION_ERROR, "Meet expected duration should be between 1 - 8 hours");
        }
    }

    public static UUID removeGuest(Meet meet, UUID userId) {
        if (meet.getGuests() == null) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Meet is empty");
        }
        Optional<Guest> guestToRemove = meet.getGuests().stream().filter(guest -> guest.getUserId().equals(userId)).findFirst();
        if (guestToRemove.isEmpty()) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Guest not found");
        }
        meet.getGuests().removeIf(guest -> guest.getUserId().equals(userId));
        meet.setCurrentPeople(meet.getCurrentPeople() - 1);
        return guestToRemove.get().getId();
    }

    public static void addUserToBlackList(Meet meet, UUID userId) {
        if (meet.getUserBlackList() == null) {
            meet.setUserBlackList(new HashSet<>());
        }
        meet.getUserBlackList().add(userId);
    }

    public static void updateState(Meet meet, MeetStatus target) {
        if (!applicableTransitions.contains(meet.getMeetStatus().getValue() + target.getValue())) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "This state not available");
        }
        meet.setMeetStatus(target);
    }

    public static Pair<Pair<Double, Double>, Pair<Double, Double>> calculateBorder(double lat, double lon, double radius) {
        double EARTH_RADIUS = 6371210;

//        double latRad = Math.toRadians(lat);
//
//        double delta_lat = radius / EARTH_RADIUS;
//        double delta_lon = radius / (EARTH_RADIUS * Math.cos(latRad));

        double delta_lat = radius / (Math.PI / 180 * EARTH_RADIUS);
        double delta_lon = radius / (Math.PI / 180 * EARTH_RADIUS * Math.cos(Math.toRadians(lon)));

        double min_lat = lat - delta_lat;
        double max_lat = lat + delta_lat;
        double min_lon = lon - delta_lon;
        double max_lon = lon + delta_lon;

        return Pair.of(Pair.of(min_lat, min_lon), Pair.of(max_lat, max_lon));

    }

}
