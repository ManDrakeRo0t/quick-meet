package ru.bogatov.quickmeet.model.enums;

public enum IsAdultFilter {

    ADULT_ONLY("ADULT_ONLY"),
    UNDERAGE_ONLY("UNDERAGE_ONLY"),
    ALL("ALL");

    private final String value;

    public String getValue() {
        return value;
    }

    IsAdultFilter(String value) {
        this.value = value;
    }

}
