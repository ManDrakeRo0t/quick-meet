package ru.bogatov.quickmeet.model.enums;

public enum AccountClass {

    BASE("base"),
    GOLD("gold");

    AccountClass(String type) {
        this.type = type;
    }

    private String type;
}
