package com.rockranger.chat.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DeletionRequestRequest {

    private String password; // Required for LOCAL accounts

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @NotBlank(message = "Confirmation is required")
    @Pattern(regexp = "^DELETE MY ACCOUNT$", message = "You must enter exact text 'DELETE MY ACCOUNT'")
    private String confirm;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getConfirm() { return confirm; }
    public void setConfirm(String confirm) { this.confirm = confirm; }
}
