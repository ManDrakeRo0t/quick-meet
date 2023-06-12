package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.Data;

@Data
public class LoginForm {
    @NotNull
    private String phoneNumber;
    @NotNull
    private String password;
}
