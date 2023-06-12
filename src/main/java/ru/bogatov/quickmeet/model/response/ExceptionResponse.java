package ru.bogatov.quickmeet.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionResponse {
    StackTraceElement[] error;
    String code;
    String message;
    String defaultMessage;
}
