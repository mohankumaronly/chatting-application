package com.rockranger.chat.common.security;

import com.rockranger.chat.user.domain.UserStatus;

import java.util.UUID;

public class CurrentUser {

    private final UUID userId;
    private final UUID sessionId;
    private final String email;
    private final String username;
    private final UserStatus status;

    public CurrentUser(UUID userId, UUID sessionId, String email, String username, UserStatus status) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.email = email;
        this.username = username;
        this.status = status;
    }

    public UUID getUserId() { return userId; }
    public UUID getSessionId() { return sessionId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public UserStatus getStatus() { return status; }
}
