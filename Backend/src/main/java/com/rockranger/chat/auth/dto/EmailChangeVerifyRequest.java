package com.rockranger.chat.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailChangeVerifyRequest {

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
