package ru.bogatov.notificationservice.model.request;

import lombok.Data;
import ru.bogatov.notificationservice.model.enums.VerificationSourceType;
import ru.bogatov.notificationservice.model.enums.VerificationStep;
@Data
public class VerificationEventBody {
    private String source;
    private VerificationSourceType verificationType;
    private VerificationStep verificationStep;
    private String code;
}
