package ru.bogatov.quickmeet.enums;

public enum MeetStatus {
    PLANNED("PLANNED"), ACTIVE("ACTIVE"), CANCELED("CANCELED"), FINISHED("FINISHED");

    private String value;

    MeetStatus(String value) {
        this.value = value;
    }
}
