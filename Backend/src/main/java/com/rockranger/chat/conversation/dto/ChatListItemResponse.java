package com.rockranger.chat.conversation.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatListItemResponse(
        UUID conversationId,
        String type,
        OtherUserSummary otherUser,
        LastMessageSummary lastMessage,
        long unreadCount,
        boolean archived,
        boolean pinned,
        Instant mutedUntil
) {
    public record OtherUserSummary(
            UUID userId,
            String username,
            String displayName,
            String avatarUrl
    ) {}

    public record LastMessageSummary(
            UUID messageId,
            String type,
            String preview,
            UUID senderId,
            Instant createdAt
    ) {}
}
