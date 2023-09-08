package ru.bogatov.quickmeet.entity.auth;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "verification_record")
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 5)
    private VerificationSourceType type;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "source", unique = true, length = 30)
    private String source;

    @Column(name = "code", nullable = false, length = 4)
    private String activationCode;

    @Column(name = "actual_to")
    private LocalDateTime actualTo;

    @Column(name = "retry_after")
    private LocalDateTime retryAfter;
}
