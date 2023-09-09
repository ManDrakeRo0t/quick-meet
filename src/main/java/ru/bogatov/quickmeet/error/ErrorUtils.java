package ru.bogatov.quickmeet.error;

import org.springframework.beans.factory.annotation.Value;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.response.ExceptionResponse;

public class ErrorUtils {

    //@Value("${error.response.stacktrace}")
    private static boolean isStackTraceEnabled = false;

    public static ApplicationException buildException(ApplicationError error) {
        return ApplicationException.builder()
                .code(error.getCode())
                .message(error.getMessage())
                .defaultMessage(error.getMessage())
                .status(error.getStatus())
                .build();
    }

    public static ApplicationException buildException(ApplicationError error, String message) {
        return ApplicationException.builder()
                .code(error.getCode())
                .message(message)
                .defaultMessage(error.getMessage())
                .status(error.getStatus())
                .build();
    }

    public static ApplicationException buildException(ApplicationError error, Exception initial, String message) {
        ApplicationException applicationException = ApplicationException.builder()
                .code(error.getCode())
                .message(message)
                .defaultMessage(initial.getMessage())
                .status(error.getStatus())
                .build();
        applicationException.setStackTrace(initial.getStackTrace());
        return applicationException;
    }

    public static ExceptionResponse buildResponse(ApplicationException ex) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .defaultMessage(ex.getDefaultMessage()).build();
        if (isStackTraceEnabled) {
            response.setError(ex.getStackTrace());
            ex.printStackTrace();
        }
        return response;
    }

    public static ExceptionResponse buildResponse(RuntimeException ex) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(ApplicationError.COMMON_ERROR.getCode())
                .message(ex.getMessage())
                .defaultMessage(ApplicationError.COMMON_ERROR.getMessage()).build();
        if (isStackTraceEnabled) {
            response.setError(ex.getStackTrace());
            ex.printStackTrace();
        }
        return response;
    }

}
