package com.rockranger.chat.block.dto;

import java.time.Instant;
import java.util.UUID;

public class BlockResponse {

    private UUID id;
    private UUID blockedUserId;
    private Instant createdAt;

    public BlockResponse() {}

    public BlockResponse(UUID id, UUID blockedUserId, Instant createdAt) {
        this.id = id;
        this.blockedUserId = blockedUserId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBlockedUserId() { return blockedUserId; }
    public void setBlockedUserId(UUID blockedUserId) { this.blockedUserId = blockedUserId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
