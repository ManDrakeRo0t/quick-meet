package ru.bogatov.quickmeet.model.enums;

public enum AccountClass {

    BASE("base"),
    PREMIUM("premium"),
    VIP("vip"),
    BUSINESS("business");

    AccountClass(String type) {
        this.type = type;
    }

    private String type;
}
