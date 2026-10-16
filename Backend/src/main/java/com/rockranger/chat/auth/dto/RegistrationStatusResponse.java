package com.rockranger.chat.auth.dto;

import java.util.UUID;

public class RegistrationStatusResponse {

    private UUID registrationId;
    private String email;
    private String status;
    private String nextStep;
    private long expiresInSeconds;

    public RegistrationStatusResponse() {}

    public RegistrationStatusResponse(UUID registrationId, String email, String status, String nextStep, long expiresInSeconds) {
        this.registrationId = registrationId;
        this.email = email;
        this.status = status;
        this.nextStep = nextStep;
        this.expiresInSeconds = expiresInSeconds;
    }

    public UUID getRegistrationId() { return registrationId; }
    public void setRegistrationId(UUID registrationId) { this.registrationId = registrationId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}
