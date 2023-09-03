package ru.bogatov.quickmeet.entity.auth;

import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.Role;

import java.io.Serializable;
import java.util.UUID;

public interface UserForAuth {
    UUID getId();
    String getPhoneNumber();
    String getPassword();
    String getRefresh();
    boolean isActive();
    boolean isBlocked();
    Role getRole();
}
