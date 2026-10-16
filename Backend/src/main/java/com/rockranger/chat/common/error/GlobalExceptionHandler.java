package com.rockranger.chat.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String traceId = UUID.randomUUID().toString();
        log.warn("Business error [{}]: {} (traceId: {})", errorCode.name(), ex.getMessage(), traceId);

        ApiError error = new ApiError(
                "https://api.chat.local/errors/" + errorCode.name().toLowerCase(),
                errorCode.name(),
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                traceId
        );
        if (ex.getDetails() != null) {
            error.setExtensions(ex.getDetails());
        }
        return ResponseEntity.status(errorCode.getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        List<ApiError.FieldErrorItem> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ApiError.FieldErrorItem(
                    fieldError.getField(),
                    fieldError.getCode() != null ? fieldError.getCode().toUpperCase() : "INVALID",
                    fieldError.getDefaultMessage()
            ));
        }

        ApiError error = new ApiError(
                "https://api.chat.local/errors/validation",
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                "One or more fields failed validation.",
                traceId
        );
        error.setFieldErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        ApiError error = new ApiError(
                "https://api.chat.local/errors/forbidden",
                "Forbidden",
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.FORBIDDEN.name(),
                "You do not have permission to access this resource.",
                traceId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        ApiError error = new ApiError(
                "https://api.chat.local/errors/unauthorized",
                "Unauthorized",
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED.name(),
                "Authentication is required or invalid.",
                traceId
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled server exception (traceId: {}):", traceId, ex);

        ApiError error = new ApiError(
                "https://api.chat.local/errors/internal-server-error",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "An unexpected internal error occurred. Please contact support with trace ID.",
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
