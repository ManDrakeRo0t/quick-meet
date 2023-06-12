package ru.bogatov.quickmeet.entities.auth;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
public class PhoneNumberVerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "phone", unique = true)
    private String phoneNumber;
    @Column(name = "mail", unique = true)
    private String mail;
    @Column(name = "code", nullable = false)
    private String activationCode;
}
