package ru.bogatov.notificationservice.model.enums;

public enum VerificationStep {
    REGISTRATION("REGISTRATION"), VERIFICATION("VERIFICATION");

    private final String value;

    public String getValue() {
        return value;
    }

    VerificationStep(String value) {
        this.value = value;
    }
}
