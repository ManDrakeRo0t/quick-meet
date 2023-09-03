package ru.bogatov.quickmeet.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Data
public class RegistrationBody {

    @NotNull
    private String firstName;
    @NotNull
    private String secondName;
    private String lastName;
    @NotNull
    private String phoneNumber;
    @NotNull
    private String password;
    private String email;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthDate;
    private Boolean isAdmin;
}


