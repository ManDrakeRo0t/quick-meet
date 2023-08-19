package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.Data;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;
import ru.bogatov.quickmeet.model.enums.VerificationStep;
@Data
public class VerificationBody {
    @NotNull
    private final String source;
    @NotNull
    private final VerificationSourceType verificationType;
    @NotNull
    private final VerificationStep verificationStep;
    private String code;
}
