package com.rockranger.chat.auth.dto;

import java.util.UUID;

public class RegistrationCompleteResponse {

    private UUID registrationId;
    private String status;
    private String nextStep;

    public RegistrationCompleteResponse() {}

    public RegistrationCompleteResponse(UUID registrationId, String status, String nextStep) {
        this.registrationId = registrationId;
        this.status = status;
        this.nextStep = nextStep;
    }

    public UUID getRegistrationId() { return registrationId; }
    public void setRegistrationId(UUID registrationId) { this.registrationId = registrationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
}
