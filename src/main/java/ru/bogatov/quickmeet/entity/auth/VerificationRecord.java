package ru.bogatov.quickmeet.entity.auth;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Enumerated(EnumType.STRING)
    private VerificationSourceType type;
    private Boolean isVerified;
    @Column(name = "source", unique = true)
    private String source;
    @Column(name = "code", nullable = false)
    private String activationCode;
}
