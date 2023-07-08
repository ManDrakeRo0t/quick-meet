package ru.bogatov.quickmeet.model.request;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.Role;

@Data
public class UserUpdateBody {
    String firstName;
    String secondName;
    String email;
    String description;
    boolean isDeleted;
    Role role;
}
