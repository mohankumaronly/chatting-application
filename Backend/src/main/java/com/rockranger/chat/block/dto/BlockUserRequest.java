package com.rockranger.chat.block.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class BlockUserRequest {

    @NotNull(message = "Blocked user ID is required")
    private UUID blockedUserId;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;

    public UUID getBlockedUserId() { return blockedUserId; }
    public void setBlockedUserId(UUID blockedUserId) { this.blockedUserId = blockedUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
