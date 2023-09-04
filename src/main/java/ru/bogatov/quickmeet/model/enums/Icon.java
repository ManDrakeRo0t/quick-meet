package ru.bogatov.quickmeet.model.enums;

public enum Icon {
    DEFAULT("DEFAULT"),
    ICON_ONE("ICON_ONE");

    private String value;

    public String getValue() {
        return value;
    }

    Icon(String value) {
        this.value = value;
    }
}
