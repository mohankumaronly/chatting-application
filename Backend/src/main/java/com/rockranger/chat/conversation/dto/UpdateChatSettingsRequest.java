package com.rockranger.chat.conversation.dto;

import java.time.Instant;

public record UpdateChatSettingsRequest(
        Boolean archived,
        Boolean pinned,
        Instant mutedUntil
) {}
