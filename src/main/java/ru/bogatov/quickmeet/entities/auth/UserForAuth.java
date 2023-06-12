package ru.bogatov.quickmeet.entities.auth;

public interface UserForAuth {
    String getId();
    String getAccountClass();
    String getPhoneNumber();
    String getRefresh();
    boolean isActive();
    boolean isBlocked();
}
