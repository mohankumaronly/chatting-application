package com.rockranger.chat.auth.dto;

public class OtpVerifyResponse {

    private String status;
    private String nextStep;

    public OtpVerifyResponse() {}

    public OtpVerifyResponse(String status, String nextStep) {
        this.status = status;
        this.nextStep = nextStep;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
}
