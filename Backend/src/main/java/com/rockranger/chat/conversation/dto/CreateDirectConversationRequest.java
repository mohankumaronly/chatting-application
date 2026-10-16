package com.rockranger.chat.conversation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDirectConversationRequest(
        @NotNull(message = "targetUserId is required")
        UUID targetUserId
) {}
