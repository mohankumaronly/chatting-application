package com.rockranger.chat.account.dto;

public class DeletionStatusResponse {

    private boolean recoverable;

    public DeletionStatusResponse() {}

    public DeletionStatusResponse(boolean recoverable) {
        this.recoverable = recoverable;
    }

    public boolean isRecoverable() { return recoverable; }
    public void setRecoverable(boolean recoverable) { this.recoverable = recoverable; }
}
