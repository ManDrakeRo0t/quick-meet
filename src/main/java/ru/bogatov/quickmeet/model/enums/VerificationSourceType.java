package ru.bogatov.quickmeet.model.enums;

public enum VerificationSourceType {
    MAIL("MAIL"), PHONE("PHONE");

    private final String value;

    public String getValue() {
        return value;
    }

    VerificationSourceType(String value) {
        this.value = value;
    }
}
