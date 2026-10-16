package com.rockranger.chat.account.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_deletion_requests")
public class AccountDeletionRequest {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "recoverable_until", nullable = false)
    private Instant recoverableUntil;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountDeletionStatus status;

    @Column(name = "recovery_token_hash", length = 255)
    private String recoveryTokenHash;

    @Column(name = "recovery_token_expires_at")
    private Instant recoveryTokenExpiresAt;

    public AccountDeletionRequest() {}

    public AccountDeletionRequest(UUID id, UUID userId, Instant recoverableUntil, String reason, String recoveryTokenHash, Instant recoveryTokenExpiresAt) {
        this.id = id;
        this.userId = userId;
        this.requestedAt = Instant.now();
        this.recoverableUntil = recoverableUntil;
        this.reason = reason;
        this.status = AccountDeletionStatus.PENDING;
        this.recoveryTokenHash = recoveryTokenHash;
        this.recoveryTokenExpiresAt = recoveryTokenExpiresAt;
    }

    public boolean isRecoverable() {
        return status == AccountDeletionStatus.PENDING && Instant.now().isBefore(recoverableUntil);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getRecoverableUntil() { return recoverableUntil; }
    public void setRecoverableUntil(Instant recoverableUntil) { this.recoverableUntil = recoverableUntil; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public AccountDeletionStatus getStatus() { return status; }
    public void setStatus(AccountDeletionStatus status) { this.status = status; }
    public String getRecoveryTokenHash() { return recoveryTokenHash; }
    public void setRecoveryTokenHash(String recoveryTokenHash) { this.recoveryTokenHash = recoveryTokenHash; }
    public Instant getRecoveryTokenExpiresAt() { return recoveryTokenExpiresAt; }
    public void setRecoveryTokenExpiresAt(Instant recoveryTokenExpiresAt) { this.recoveryTokenExpiresAt = recoveryTokenExpiresAt; }
}
