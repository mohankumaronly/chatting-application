package com.rockranger.chat.messaging.application;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageRepository;
import com.rockranger.chat.messaging.domain.MessageType;
import com.rockranger.chat.messaging.dto.EditMessageRequest;
import com.rockranger.chat.messaging.dto.MessageResponse;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import com.rockranger.chat.realtime.publisher.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class MessageEditService {

    public static final Duration EDIT_TIME_LIMIT = Duration.ofMinutes(15);

    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final RealtimePublisher realtimePublisher;

    public MessageEditService(
            MessageRepository messageRepository,
            MessageService messageService,
            RealtimePublisher realtimePublisher
    ) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public MessageResponse editMessage(UUID currentUserId, UUID messageId, EditMessageRequest req) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        if (!message.getSenderId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.MESSAGE_EDIT_FORBIDDEN, "Only the sender can edit this message");
        }

        if (message.isDeleted()) {
            throw new ApiException(ErrorCode.MESSAGE_EDIT_EXPIRED, "Cannot edit a deleted message");
        }

        if (message.getMessageType() != MessageType.TEXT) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Only text messages can be edited");
        }

        if (message.getCreatedAt().plus(EDIT_TIME_LIMIT).isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.MESSAGE_EDIT_EXPIRED, "Edit window has expired (15 minutes)");
        }

        if (req.content().length() > 4000) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Content exceeds 4000 characters limit");
        }

        message.setContent(req.content());
        message.setEditedAt(Instant.now());
        messageRepository.save(message);

        MessageResponse response = messageService.buildResponse(message);

        RealtimeEvent event = RealtimeEvent.of("message.updated", message.getConversationId(), response);
        realtimePublisher.recordOutboxEvent("message.updated", "CONVERSATION", message.getConversationId(), event);
        realtimePublisher.publishToConversation(message.getConversationId(), event);

        return response;
    }
}
