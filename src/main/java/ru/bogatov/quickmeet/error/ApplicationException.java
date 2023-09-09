package ru.bogatov.quickmeet.error;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ApplicationException extends RuntimeException {
    public String message;
    public String defaultMessage;
    public HttpStatus status;
    public String code;
}
