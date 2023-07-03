package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class LoginAfterVerificationForm {
    @NotNull
    private String phoneNumber;
}
