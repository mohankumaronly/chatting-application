package com.rockranger.chat.user.domain;

public enum UserStatus {
    PENDING_SETUP,
    ACTIVE,
    SUSPENDED,
    PENDING_DELETION,
    DEACTIVATED;

    public boolean canAuthenticate() {
        return this == ACTIVE || this == PENDING_SETUP;
    }

    public boolean isOperational() {
        return this == ACTIVE;
    }
}
