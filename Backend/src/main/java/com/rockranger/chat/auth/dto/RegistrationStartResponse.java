package com.rockranger.chat.auth.dto;

import java.util.UUID;

public class RegistrationStartResponse {

    private UUID registrationId;
    private String challengeToken;
    private String status;
    private long expiresInSeconds;
    private String nextStep;

    public RegistrationStartResponse() {}

    public RegistrationStartResponse(UUID registrationId, String challengeToken, String status, long expiresInSeconds, String nextStep) {
        this.registrationId = registrationId;
        this.challengeToken = challengeToken;
        this.status = status;
        this.expiresInSeconds = expiresInSeconds;
        this.nextStep = nextStep;
    }

    public UUID getRegistrationId() { return registrationId; }
    public void setRegistrationId(UUID registrationId) { this.registrationId = registrationId; }
    public String getChallengeToken() { return challengeToken; }
    public void setChallengeToken(String challengeToken) { this.challengeToken = challengeToken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
}
