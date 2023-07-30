package ru.bogatov.quickmeet.model.enums;

public enum MeetUpdateEventType {
    USER_LEFT("USER_LEFT"),
    USER_JOINED("USER_JOINED"),
    USER_DELETED("USER_DELETED"),
    MEET_UPDATED("MEET_UPDATED"),
    MEET_CREATED("MEET_CREATED");

    private String type;

    public String getValue() {
        return type;
    }

    MeetUpdateEventType(String value) {
        this.type = value;
    }
}
