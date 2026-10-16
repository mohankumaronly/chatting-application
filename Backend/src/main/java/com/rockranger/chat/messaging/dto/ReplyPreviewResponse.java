package com.rockranger.chat.messaging.dto;

import java.util.UUID;

public record ReplyPreviewResponse(
        UUID messageId,
        UUID senderId,
        String type,
        String preview
) {}
