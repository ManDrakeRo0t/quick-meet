package ru.bogatov.quickmeet.services.util;

import org.springframework.data.util.Pair;
import ru.bogatov.quickmeet.entities.Meet;
import ru.bogatov.quickmeet.entities.User;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public static void checkIsGuestApplicable(Meet meet, User guest) {
        if (meet.getOwner().getId().equals(guest.getId())) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Owner can not be guest");
        }
        if (meet.getUserBlackList() != null && !meet.getUserBlackList().isEmpty() && meet.getUserBlackList().contains(guest.getId())) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "User in black list");
        }
    }

    public static void checkIsFreeAndThrow(Meet meet) {
        if (meet.getMaxPeople() != 0 && meet.getCurrentPeople() >= meet.getMaxPeople()) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Meet is full");
        }
    }

    public static void addGuest(Meet meet, User user) {
        if (meet.getGuests() == null) {
            meet.setGuests(new HashSet<>());
        }
        meet.getGuests().add(user);
        meet.setCurrentPeople(meet.getCurrentPeople() + 1);
    }

    public static void removeGuest(Meet meet, User user) {
        if (meet.getGuests() == null) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Meet is empty");
        }
        if (!meet.getGuests().removeIf(guest -> guest.getId().equals(user.getId()))) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_MEET_ERROR, "Guest not found");
        }
        meet.setCurrentPeople(meet.getCurrentPeople() - 1);
    }

    public static void addUserToBlackList(Meet meet, User user) {
        if (meet.getUserBlackList() == null) {
            meet.setUserBlackList(new ArrayList<>());
        }
        meet.getUserBlackList().add(user.getId());
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
