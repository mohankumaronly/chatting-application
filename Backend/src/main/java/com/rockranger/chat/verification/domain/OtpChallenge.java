package com.rockranger.chat.verification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_challenges")
public class OtpChallenge {

    @Id
    private UUID id;

    @Column(name = "registration_id")
    private UUID registrationId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChallengePurpose purpose;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public OtpChallenge() {}

    public OtpChallenge(UUID id, UUID registrationId, UUID userId, ChallengePurpose purpose, String otpHash, Instant expiresAt) {
        this.id = id;
        this.registrationId = registrationId;
        this.userId = userId;
        this.purpose = purpose;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRegistrationId() { return registrationId; }
    public void setRegistrationId(UUID registrationId) { this.registrationId = registrationId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public ChallengePurpose getPurpose() { return purpose; }
    public void setPurpose(ChallengePurpose purpose) { this.purpose = purpose; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
