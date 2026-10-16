package com.rockranger.chat.presence.dto;

import java.time.Instant;
import java.util.UUID;

public record UserPresenceSettingsResponse(
        UUID userId,
        String lastSeenVisibility,
        String onlineVisibility,
        boolean readReceiptsEnabled,
        boolean typingVisibility,
        String profilePhotoVisibility,
        Instant updatedAt
) {}
