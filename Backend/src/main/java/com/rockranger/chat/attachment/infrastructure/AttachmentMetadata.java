package com.rockranger.chat.attachment.infrastructure;

public record AttachmentMetadata(
        String publicId,
        String format,
        long bytes,
        Integer width,
        Integer height,
        Integer durationMs,
        String resourceType
) {}
