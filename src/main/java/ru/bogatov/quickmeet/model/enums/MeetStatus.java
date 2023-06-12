package ru.bogatov.quickmeet.model.enums;

public enum MeetStatus {
    PLANNED("PLANNED"), ACTIVE("ACTIVE"), CANCELED("CANCELED"), FINISHED("FINISHED");

    private String value;

    public String getValue() {
        return value;
    }

    MeetStatus(String value) {
        this.value = value;
    }
}
