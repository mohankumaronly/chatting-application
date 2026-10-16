package com.rockranger.chat.realtime.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReadCursorCommand(
        @NotNull(message = "conversationId is required")
        UUID conversationId,
        UUID throughMessageId,
        Long throughSequenceNumber
) {}
