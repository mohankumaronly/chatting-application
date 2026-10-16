package com.rockranger.chat.conversation.dto;

import java.util.List;
import java.util.UUID;

public record UserSearchResponse(
        List<Item> items,
        String nextCursor
) {
    public record Item(
            UUID userId,
            String username,
            String displayName,
            String avatarUrl,
            String bio
    ) {}
}
