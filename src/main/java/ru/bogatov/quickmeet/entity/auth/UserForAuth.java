package ru.bogatov.quickmeet.entity.auth;

public interface UserForAuth {
    String getId();
    String getAccountClass();
    String getPhoneNumber();
    String getRefresh();
    boolean isActive();
    boolean isBlocked();
}
