package com.rockranger.chat.realtime.dto;

import com.rockranger.chat.attachment.dto.AttachmentReferenceRequest;
import com.rockranger.chat.messaging.domain.MessageType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record WebSocketSendMessageCommand(
        @NotNull UUID conversationId,
        @NotNull UUID clientMessageId,
        @NotNull MessageType type,
        String content,
        UUID replyToMessageId,
        List<AttachmentReferenceRequest> attachments
) {}
