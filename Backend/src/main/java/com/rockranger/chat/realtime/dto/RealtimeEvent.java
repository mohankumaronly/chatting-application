package com.rockranger.chat.realtime.dto;

import java.time.Instant;
import java.util.UUID;

public record RealtimeEvent(
        UUID eventId,
        String type,
        UUID conversationId,
        Instant occurredAt,
        Object data
) {
    public static RealtimeEvent of(String type, UUID conversationId, Object data) {
        return new RealtimeEvent(UUID.randomUUID(), type, conversationId, Instant.now(), data);
    }
}
