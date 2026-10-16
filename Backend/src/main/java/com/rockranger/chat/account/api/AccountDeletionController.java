package com.rockranger.chat.account.api;

import com.rockranger.chat.account.application.AccountDeletionService;
import com.rockranger.chat.account.dto.DeletionRequestRequest;
import com.rockranger.chat.account.dto.DeletionRequestResponse;
import com.rockranger.chat.account.dto.DeletionStatusRequest;
import com.rockranger.chat.account.dto.DeletionStatusResponse;
import com.rockranger.chat.account.dto.RecoveryRequest;
import com.rockranger.chat.auth.dto.AuthResponse;
import com.rockranger.chat.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountDeletionController {

    private final AccountDeletionService deletionService;

    public AccountDeletionController(AccountDeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @PostMapping("/api/v1/me/deletion")
    public ResponseEntity<DeletionRequestResponse> requestDeletion(@AuthenticationPrincipal CurrentUser currentUser,
                                                                   @Valid @RequestBody DeletionRequestRequest request,
                                                                   HttpServletResponse response) {
        DeletionRequestResponse deletionResponse = deletionService.requestDeletion(currentUser, request, response);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(deletionResponse);
    }

    @GetMapping("/api/v1/me/deletion")
    public ResponseEntity<DeletionRequestResponse> getDeletionInfo(@AuthenticationPrincipal CurrentUser currentUser) {
        DeletionRequestResponse deletionResponse = deletionService.getDeletionInfo(currentUser);
        return ResponseEntity.ok(deletionResponse);
    }

    @PostMapping("/api/v1/auth/deletion/recover")
    public ResponseEntity<AuthResponse> recoverAccount(@Valid @RequestBody RecoveryRequest request,
                                                       HttpServletResponse response) {
        AuthResponse authResponse = deletionService.recoverAccount(request.getRecoveryToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/api/v1/auth/deletion/status")
    public ResponseEntity<DeletionStatusResponse> checkDeletionStatus(@Valid @RequestBody DeletionStatusRequest request,
                                                                      HttpServletRequest httpRequest) {
        DeletionStatusResponse response = deletionService.checkDeletionStatus(request.getEmail(), httpRequest);
        return ResponseEntity.ok(response);
    }
}
