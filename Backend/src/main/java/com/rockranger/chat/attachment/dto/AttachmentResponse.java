package com.rockranger.chat.attachment.dto;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID attachmentId,
        UUID ownerId,
        String resourceType,
        String mimeType,
        long sizeBytes,
        String originalFilename,
        Integer width,
        Integer height,
        Integer durationMs,
        String status,
        String downloadUrl,
        Instant createdAt
) {}
