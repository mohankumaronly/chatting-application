package com.rockranger.chat.conversation.dto;

import java.util.List;

public record ChatListResponse(
        List<ChatListItemResponse> items,
        String nextCursor
) {}
