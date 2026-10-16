package com.rockranger.chat.conversation.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationMemberResponse(
        UUID userId,
        String role,
        Instant joinedAt
) {}
