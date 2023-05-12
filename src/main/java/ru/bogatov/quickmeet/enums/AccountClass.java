package ru.bogatov.quickmeet.enums;

public enum AccountClass {

    BASE("base"),
    GOLD("gold");

    AccountClass(String type) {
        this.type = type;
    }

    private String type;
}
