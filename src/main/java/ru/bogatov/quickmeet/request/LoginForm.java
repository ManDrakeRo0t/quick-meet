package ru.bogatov.quickmeet.request;

import lombok.Data;

@Data
public class LoginForm {
    private String phoneNumber;
    private String password;
}
