package com.rockranger.chat.auth.api;

import com.rockranger.chat.auth.application.PasswordRecoveryService;
import com.rockranger.chat.auth.dto.ForgotPasswordRequest;
import com.rockranger.chat.auth.dto.PasswordChangeRequest;
import com.rockranger.chat.auth.dto.PasswordResetRequest;
import com.rockranger.chat.auth.dto.ResetVerifyRequest;
import com.rockranger.chat.auth.dto.ResetVerifyResponse;
import com.rockranger.chat.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest httpRequest) {
        passwordRecoveryService.forgotPassword(request.getEmail(), httpRequest);
        return ResponseEntity.ok(Map.of("message", "If an account matches this email, a verification code has been sent."));
    }

    @PostMapping("/reset/verify")
    public ResponseEntity<ResetVerifyResponse> verifyResetOtp(@Valid @RequestBody ResetVerifyRequest request) {
        ResetVerifyResponse response = passwordRecoveryService.verifyResetOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset. Please log in with your new password."));
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal CurrentUser currentUser,
                                                              @Valid @RequestBody PasswordChangeRequest request) {
        passwordRecoveryService.changePassword(currentUser, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully. Please log in again."));
    }
}
