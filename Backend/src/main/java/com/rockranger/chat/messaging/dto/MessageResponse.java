package com.rockranger.chat.messaging.dto;

import com.rockranger.chat.attachment.dto.AttachmentResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        UUID clientMessageId,
        String type,
        String content,
        long sequenceNumber,
        ReplyPreviewResponse replyTo,
        List<AttachmentResponse> attachments,
        Instant createdAt,
        Instant editedAt,
        boolean deleted
) {}
