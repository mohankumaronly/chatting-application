package com.rockranger.chat.auth.dto;

import java.time.Instant;
import java.util.UUID;

public class SessionResponse {

    private UUID id;
    private String deviceName;
    private String clientType;
    private Instant createdAt;
    private Instant lastUsedAt;
    private boolean current;

    public SessionResponse() {}

    public SessionResponse(UUID id, String deviceName, String clientType, Instant createdAt, Instant lastUsedAt, boolean current) {
        this.id = id;
        this.deviceName = deviceName;
        this.clientType = clientType;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.current = current;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
}
