package ru.bogatov.quickmeet.request;

import lombok.Data;
import ru.bogatov.quickmeet.entities.City;

import java.util.Date;

@Data
public class RegistrationBody {

    private String firstName;

    private String secondName;

    private String lastName;

    private String phoneNumber;

    private String password;

    private String email;

    private Date birthDate;

    private City city;

}
