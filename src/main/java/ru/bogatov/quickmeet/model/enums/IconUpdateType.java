package ru.bogatov.quickmeet.model.enums;

public enum IconUpdateType {

    BASE("BASE"),
    CUSTOM("CUSTOM"),
    NONE("NONE");

    private String value;

    public String getValue() {
        return value;
    }

    IconUpdateType(String value) {
        this.value = value;
    }

}
