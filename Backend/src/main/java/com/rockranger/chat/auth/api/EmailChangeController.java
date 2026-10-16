package com.rockranger.chat.auth.api;

import com.rockranger.chat.auth.application.EmailChangeService;
import com.rockranger.chat.auth.dto.EmailChangeRequest;
import com.rockranger.chat.auth.dto.EmailChangeVerifyRequest;
import com.rockranger.chat.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/email")
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    public EmailChangeController(EmailChangeService emailChangeService) {
        this.emailChangeService = emailChangeService;
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> startEmailChange(@AuthenticationPrincipal CurrentUser currentUser,
                                                                @Valid @RequestBody EmailChangeRequest request) {
        emailChangeService.startEmailChange(currentUser, request.getNewEmail());
        return ResponseEntity.ok(Map.of("message", "A verification code has been sent to your new email address."));
    }

    @PostMapping("/change/verify")
    public ResponseEntity<Map<String, String>> verifyEmailChange(@AuthenticationPrincipal CurrentUser currentUser,
                                                                 @Valid @RequestBody EmailChangeVerifyRequest request) {
        emailChangeService.verifyEmailChange(currentUser, request.getNewEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Email address has been successfully updated."));
    }
}
