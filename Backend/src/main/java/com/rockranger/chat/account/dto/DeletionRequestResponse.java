package com.rockranger.chat.account.dto;

import java.time.Instant;

public class DeletionRequestResponse {

    private String status;
    private Instant recoverableUntil;
    private String message;

    public DeletionRequestResponse() {}

    public DeletionRequestResponse(String status, Instant recoverableUntil, String message) {
        this.status = status;
        this.recoverableUntil = recoverableUntil;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getRecoverableUntil() { return recoverableUntil; }
    public void setRecoverableUntil(Instant recoverableUntil) { this.recoverableUntil = recoverableUntil; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
