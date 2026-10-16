package com.rockranger.chat.messaging.dto;

import java.util.List;

public record MessageHistoryResponse(
        List<MessageResponse> items,
        Long nextCursor,
        boolean hasMore
) {}
