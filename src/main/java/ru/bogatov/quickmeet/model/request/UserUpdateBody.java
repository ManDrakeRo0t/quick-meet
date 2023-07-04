package ru.bogatov.quickmeet.model.request;

import lombok.Data;

@Data
public class UserUpdateBody {
    String firstName;
    String secondName;
    String email;
    String description;
    boolean isDeleted;
}
