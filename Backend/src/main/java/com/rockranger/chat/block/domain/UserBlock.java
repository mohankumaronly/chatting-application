package com.rockranger.chat.block.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_blocks", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_block", columnNames = {"blocker_user_id", "blocked_user_id"})
})
public class UserBlock {

    @Id
    private UUID id;

    @Column(name = "blocker_user_id", nullable = false)
    private UUID blockerUserId;

    @Column(name = "blocked_user_id", nullable = false)
    private UUID blockedUserId;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UserBlock() {}

    public UserBlock(UUID id, UUID blockerUserId, UUID blockedUserId, String reason) {
        this.id = id;
        this.blockerUserId = blockerUserId;
        this.blockedUserId = blockedUserId;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBlockerUserId() { return blockerUserId; }
    public void setBlockerUserId(UUID blockerUserId) { this.blockerUserId = blockerUserId; }
    public UUID getBlockedUserId() { return blockedUserId; }
    public void setBlockedUserId(UUID blockedUserId) { this.blockedUserId = blockedUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
