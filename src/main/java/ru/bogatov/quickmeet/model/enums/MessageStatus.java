package ru.bogatov.quickmeet.model.enums;

public enum MessageStatus {
    SEND("SEND"), REMOVED("REMOVED"), DELIVERED("DELIVERED") ;


    private String value;

    MessageStatus(String value) {
        this.value = value;
    }
}
