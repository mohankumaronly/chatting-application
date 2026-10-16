package com.rockranger.chat.realtime.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TypingCommand(
        @NotNull(message = "conversationId is required")
        UUID conversationId,
        String state
) {}
