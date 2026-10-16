package com.rockranger.chat.session.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 255)
    private String refreshTokenHash;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 30)
    private ClientType clientType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_session_id")
    private UUID replacedBySessionId;

    public UserSession() {}

    public UserSession(UUID id, UUID userId, String refreshTokenHash, String deviceName, ClientType clientType, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.refreshTokenHash = refreshTokenHash;
        this.deviceName = deviceName;
        this.clientType = clientType;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.lastUsedAt = Instant.now();
    }

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public ClientType getClientType() { return clientType; }
    public void setClientType(ClientType clientType) { this.clientType = clientType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public UUID getReplacedBySessionId() { return replacedBySessionId; }
    public void setReplacedBySessionId(UUID replacedBySessionId) { this.replacedBySessionId = replacedBySessionId; }
}
