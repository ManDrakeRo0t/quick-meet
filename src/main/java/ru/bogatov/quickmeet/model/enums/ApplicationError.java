package ru.bogatov.quickmeet.model.enums;


import org.springframework.http.HttpStatus;

public enum ApplicationError {

    COMMON_ERROR("Something went wrong", "QM-0000", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_ACCESS_ERROR("Something went during access to data", "QM-0001", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_NOT_FOUND_ERROR("Data not found", "QM-0002", HttpStatus.NOT_FOUND),
    REQUEST_PARAMETERS_ERROR("Something wrong with request params", "QM-0003", HttpStatus.BAD_REQUEST),
    BUSINESS_LOGIC_ERROR("Common error", "QM-0004", HttpStatus.BAD_REQUEST),
    USER_EXISTS("User already exists", "QM-0100", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", "QM-0101", HttpStatus.NOT_FOUND),
    USER_IS_BLOCKED("User is blocked", "QM-0102", HttpStatus.BAD_REQUEST),
    COMMON_MEET_ERROR("Something went wrong during meet organization", "QM-0200", HttpStatus.BAD_REQUEST),
    MEET_VALIDATION_ERROR("Something went wrong during meet validation", "QM-201", HttpStatus.BAD_REQUEST),
    FILE_PROCESSING_ERROR("Error while file processing", "QM-0500", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTHENTICATION_ERROR("Error during authentication", "QM-0400", HttpStatus.UNAUTHORIZED);



    private final String message;
    private final String code;
    private final HttpStatus status;

    ApplicationError(String message, String code, HttpStatus status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
