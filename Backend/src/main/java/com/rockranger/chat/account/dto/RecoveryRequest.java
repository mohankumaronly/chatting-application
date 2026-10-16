package com.rockranger.chat.account.dto;

import jakarta.validation.constraints.NotBlank;

public class RecoveryRequest {

    @NotBlank(message = "Recovery token is required")
    private String recoveryToken;

    public String getRecoveryToken() { return recoveryToken; }
    public void setRecoveryToken(String recoveryToken) { this.recoveryToken = recoveryToken; }
}
