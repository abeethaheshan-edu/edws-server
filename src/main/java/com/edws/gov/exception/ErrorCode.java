package com.edws.gov.exception;

import org.springframework.http.HttpStatus;


public enum ErrorCode {

    VALIDATION_FAILED("EDWS-REQ-4001", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR,
            "One or more fields are invalid"),
    MALFORMED_REQUEST("EDWS-REQ-4002", HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST,
            "Request body is missing or not valid JSON"),
    INVALID_PARAMETER("EDWS-REQ-4003", HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST,
            "A request parameter has an invalid value"),
    MISSING_PARAMETER("EDWS-REQ-4004", HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST,
            "A required request parameter is missing"),

    INVALID_CREDENTIALS("EDWS-AUTH-4010", HttpStatus.UNAUTHORIZED, ErrorType.AUTHENTICATION_ERROR,
            "Invalid email or password"),
    UNAUTHENTICATED("EDWS-AUTH-4011", HttpStatus.UNAUTHORIZED, ErrorType.AUTHENTICATION_ERROR,
            "You must be signed in to access this resource"),
    TOKEN_MISSING("EDWS-AUTH-4012", HttpStatus.UNAUTHORIZED, ErrorType.AUTHENTICATION_ERROR,
            "Authorization token was not provided"),
    TOKEN_EXPIRED("EDWS-AUTH-4013", HttpStatus.UNAUTHORIZED, ErrorType.AUTHENTICATION_ERROR,
            "Your session has expired, please sign in again"),
    TOKEN_INVALID("EDWS-AUTH-4014", HttpStatus.UNAUTHORIZED, ErrorType.AUTHENTICATION_ERROR,
            "Authorization token is invalid"),
    TOKEN_TYPE_MISMATCH("EDWS-AUTH-4015", HttpStatus.UNAUTHORIZED, ErrorType.AUTHENTICATION_ERROR,
            "Wrong token type used for this operation"),
    ACCOUNT_NOT_ACTIVE("EDWS-AUTH-4016", HttpStatus.FORBIDDEN, ErrorType.AUTHENTICATION_ERROR,
            "This account is not active, please contact your administrator"),

    PASSWORD_CHANGE_REQUIRED("EDWS-AUTH-4017", HttpStatus.FORBIDDEN, ErrorType.AUTHENTICATION_ERROR,
            "You must set a new password before using this account"),
    PASSWORD_CONFIRMATION_MISMATCH("EDWS-AUTH-4018", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR,
            "New password and confirmation do not match"),
    PASSWORD_REUSED("EDWS-AUTH-4019", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR,
            "New password must be different from the current one"),
    CURRENT_PASSWORD_INCORRECT("EDWS-AUTH-4020", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR,
            "Current password is incorrect"),
    RESET_TOKEN_INVALID("EDWS-AUTH-4021", HttpStatus.BAD_REQUEST, ErrorType.AUTHENTICATION_ERROR,
            "This password reset link is not valid"),
    RESET_TOKEN_EXPIRED("EDWS-AUTH-4022", HttpStatus.BAD_REQUEST, ErrorType.AUTHENTICATION_ERROR,
            "This password reset link has expired, please request a new one"),

    ACCESS_DENIED("EDWS-AUTHZ-4030", HttpStatus.FORBIDDEN, ErrorType.AUTHORIZATION_ERROR,
            "You do not have permission to perform this action"),
    OUT_OF_ADMINISTRATIVE_SCOPE("EDWS-AUTHZ-4031", HttpStatus.FORBIDDEN, ErrorType.AUTHORIZATION_ERROR,
            "This record is outside your administrative area"),
    GN_DIVISION_NOT_ASSIGNED("EDWS-AUTHZ-4032", HttpStatus.FORBIDDEN, ErrorType.AUTHORIZATION_ERROR,
            "Your account has no GN division assigned, please contact your administrator"),

    USER_NOT_FOUND("EDWS-USER-4040", HttpStatus.NOT_FOUND, ErrorType.RESOURCE_NOT_FOUND,
            "User not found"),
    GN_DIVISION_NOT_FOUND("EDWS-GND-4041", HttpStatus.NOT_FOUND, ErrorType.RESOURCE_NOT_FOUND,
            "GN division not found"),
    PATH_NOT_FOUND("EDWS-SYS-4044", HttpStatus.NOT_FOUND, ErrorType.PATH_NOT_FOUND,
            "The requested endpoint does not exist"),
    METHOD_NOT_ALLOWED("EDWS-SYS-4045", HttpStatus.METHOD_NOT_ALLOWED, ErrorType.METHOD_NOT_ALLOWED,
            "This HTTP method is not supported on this endpoint"),
    UNSUPPORTED_MEDIA_TYPE("EDWS-SYS-4046", HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorType.UNSUPPORTED_MEDIA_TYPE,
            "The content type of the request is not supported"),

    ROLE_DEFINITION_NOT_FOUND("EDWS-SYS-4047", HttpStatus.NOT_FOUND, ErrorType.RESOURCE_NOT_FOUND,
            "No access definition exists for this role"),

    EMAIL_ALREADY_EXISTS("EDWS-USER-4090", HttpStatus.CONFLICT, ErrorType.CONFLICT,
            "An account with this email already exists"),
    NIC_ALREADY_EXISTS("EDWS-USER-4091", HttpStatus.CONFLICT, ErrorType.CONFLICT,
            "An account with this NIC already exists"),
    DUPLICATE_RESOURCE("EDWS-SYS-4092", HttpStatus.CONFLICT, ErrorType.CONFLICT,
            "This record already exists"),


    DATA_ACCESS_ERROR("EDWS-SYS-5001", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL_ERROR,
            "A database error occurred, please try again"),
    INTERNAL_ERROR("EDWS-SYS-5000", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL_ERROR,
            "Something went wrong , please try again");

    private final String code;
    private final HttpStatus status;
    private final ErrorType type;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, ErrorType type, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.type = type;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorType getType() {
        return type;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
