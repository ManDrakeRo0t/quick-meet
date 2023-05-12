package ru.bogatov.quickmeet.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

//@Entity
@Data
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    private User user;

    private String phoneNumber;

    private String description;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String refresh;

    private boolean isActive;

    private boolean isBlocked;

    @JsonIgnore
    private String activationCode;

    private String email;

    @OneToOne
    private BillingAccount billingAccount;

    private Date registrationDate;

    private Date birthDate;

    //все остальное

}
