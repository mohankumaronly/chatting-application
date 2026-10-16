package com.rockranger.chat.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid identifier or password"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred"),

    // Account & Status
    ACCOUNT_PENDING_DELETION(HttpStatus.FORBIDDEN, "This account is scheduled for deletion"),
    ACCOUNT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "Account setup is not complete"),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "Account is suspended"),
    ACCOUNT_DELETED(HttpStatus.GONE, "Account has been permanently deleted"),

    // Registration & OTP
    CHALLENGE_EXPIRED(HttpStatus.GONE, "Verification challenge has expired"),
    CHALLENGE_INVALID(HttpStatus.BAD_REQUEST, "Invalid or unauthorized registration challenge"),
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Invalid verification code"),
    OTP_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Maximum verification attempts exceeded"),
    OTP_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another code"),

    // Profile & Username
    USERNAME_ALREADY_TAKEN(HttpStatus.CONFLICT, "Username is already in use"),
    USERNAME_CHANGE_COOLDOWN(HttpStatus.CONFLICT, "Your username cannot be changed yet"),
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "Username must be 3-30 lowercase characters, numbers or underscore"),

    // Blocking
    SELF_BLOCK_FORBIDDEN(HttpStatus.BAD_REQUEST, "You cannot block yourself"),
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "Block relationship not found"),

    // Deletion
    DELETION_REQUEST_EXISTS(HttpStatus.CONFLICT, "An active deletion request already exists"),
    DELETION_RECOVERY_EXPIRED(HttpStatus.GONE, "Account recovery window has expired"),
    DELETION_RECOVERY_INVALID(HttpStatus.UNAUTHORIZED, "Invalid or expired recovery token");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
