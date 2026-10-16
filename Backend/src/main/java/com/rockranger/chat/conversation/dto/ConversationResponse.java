package com.rockranger.chat.conversation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID conversationId,
        String type,
        boolean created,
        List<ConversationMemberResponse> members,
        Instant createdAt,
        Instant lastMessageAt
) {}
