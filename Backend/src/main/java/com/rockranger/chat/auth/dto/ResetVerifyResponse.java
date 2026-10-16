package com.rockranger.chat.auth.dto;

public class ResetVerifyResponse {

    private String resetToken;
    private long expiresInSeconds;

    public ResetVerifyResponse() {}

    public ResetVerifyResponse(String resetToken, long expiresInSeconds) {
        this.resetToken = resetToken;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}
