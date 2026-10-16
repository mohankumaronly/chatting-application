package com.rockranger.chat.attachment.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UploadIntentResponse(
        UUID attachmentId,
        String uploadMethod,
        String uploadUrl,
        Map<String, Object> uploadFields,
        Instant expiresAt,
        String status
) {}
