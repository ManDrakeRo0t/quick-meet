package ru.bogatov.quickmeetmessenger.model;

public enum EventType {
    NEW_MESSAGE("NEW_MESSAGE"),
    READ_CHAT("READ_CHAT"),
    USER_DELETED("USER_DELETED"),
    USER_JOINED("USER_JOINED"),
    USER_LEFT("USER_LEFT"),
    MEET_UPDATED("MEET_UPDATED"),
    MEET_CREATED("MEET_CREATED");

    EventType(String type) {
        this.type = type;
    }
    private String type;
}
