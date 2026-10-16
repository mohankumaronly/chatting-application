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
    DELETION_RECOVERY_INVALID(HttpStatus.UNAUTHORIZED, "Invalid or expired recovery token"),

    // Chat & Messaging (Phase 2)
    CONVERSATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access to conversation denied"),
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Conversation not found"),
    USER_BLOCKED(HttpStatus.FORBIDDEN, "Cannot communicate with blocked user"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found"),
    MESSAGE_EDIT_EXPIRED(HttpStatus.BAD_REQUEST, "Message edit window has expired"),
    MESSAGE_EDIT_FORBIDDEN(HttpStatus.FORBIDDEN, "Only the sender can edit this message"),
    MESSAGE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "Not allowed to delete this message"),
    REPLY_TARGET_INVALID(HttpStatus.BAD_REQUEST, "Reply target message is invalid or from another conversation"),
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Attachment not found"),
    ATTACHMENT_NOT_READY(HttpStatus.BAD_REQUEST, "Attachment is not ready for use"),
    ATTACHMENT_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Attachment exceeds maximum allowed size"),
    ATTACHMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "Attachment verification failed"),
    MESSAGE_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Message rate limit exceeded"),
    UPLOAD_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Upload rate limit exceeded");

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
