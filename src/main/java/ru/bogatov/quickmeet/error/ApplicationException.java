package ru.bogatov.quickmeet.error;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ApplicationException extends RuntimeException {
    // todo хороший error handling
    public String message;
    public HttpStatus status;
    public String code;
}
