package com.rockranger.chat.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerifyRequest {

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit numeric code")
    private String otp;

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
