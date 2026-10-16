package com.rockranger.chat.presence.dto;

import java.time.Instant;
import java.util.UUID;

public record PresenceResponse(
        UUID userId,
        boolean online,
        Instant lastSeen
) {
    public static PresenceResponse hidden(UUID userId) {
        return new PresenceResponse(userId, false, null);
    }
}
