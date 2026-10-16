package com.rockranger.chat.messaging.dto;

import com.rockranger.chat.attachment.dto.AttachmentReferenceRequest;
import com.rockranger.chat.messaging.domain.MessageType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SendMessageRequest(
        @NotNull(message = "clientMessageId is required")
        UUID clientMessageId,

        @NotNull(message = "type is required")
        MessageType type,

        String content,

        UUID replyToMessageId,

        List<AttachmentReferenceRequest> attachments
) {}
