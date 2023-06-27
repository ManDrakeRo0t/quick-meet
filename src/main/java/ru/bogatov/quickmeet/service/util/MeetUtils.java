package ru.bogatov.quickmeet.service.util;

import org.springframework.data.util.Pair;
import ru.bogatov.quickmeet.entity.Guest;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
