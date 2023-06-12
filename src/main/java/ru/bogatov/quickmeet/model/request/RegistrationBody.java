package ru.bogatov.quickmeet.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class RegistrationBody {

    private String firstName;
    private String secondName;
    private String lastName;
    private String phoneNumber;
    private String password;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthDate;
    private String cityName;
    private UUID cityId;

}


