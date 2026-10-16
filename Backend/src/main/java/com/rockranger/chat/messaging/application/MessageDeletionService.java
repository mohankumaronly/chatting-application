package com.rockranger.chat.messaging.application;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.messaging.domain.*;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import com.rockranger.chat.realtime.publisher.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageDeletionService {

    public static final Duration DELETE_FOR_EVERYONE_LIMIT = Duration.ofHours(48);

    private final MessageRepository messageRepository;
    private final MessageUserDeletionRepository userDeletionRepository;
    private final ConversationAuthorizationService authorizationService;
    private final RealtimePublisher realtimePublisher;

    public MessageDeletionService(
            MessageRepository messageRepository,
            MessageUserDeletionRepository userDeletionRepository,
            ConversationAuthorizationService authorizationService,
            RealtimePublisher realtimePublisher
    ) {
        this.messageRepository = messageRepository;
        this.userDeletionRepository = userDeletionRepository;
        this.authorizationService = authorizationService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public void deleteMessage(UUID currentUserId, UUID messageId, DeleteScope scope) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        DeleteScope effectiveScope = scope != null ? scope : DeleteScope.FOR_ME;

        if (effectiveScope == DeleteScope.FOR_ME) {
            authorizationService.requireCanView(currentUserId, message.getConversationId());

            if (!userDeletionRepository.isMessageDeletedForUser(messageId, currentUserId)) {
                userDeletionRepository.save(new MessageUserDeletion(messageId, currentUserId));
            }

            RealtimeEvent event = RealtimeEvent.of(
                    "message.deleted",
                    message.getConversationId(),
                    Map.of("messageId", messageId, "scope", "FOR_ME")
            );
            realtimePublisher.publishToUser(currentUserId, event);

        } else if (effectiveScope == DeleteScope.FOR_EVERYONE) {
            if (!message.getSenderId().equals(currentUserId)) {
                throw new ApiException(ErrorCode.MESSAGE_DELETE_FORBIDDEN, "Only the sender can delete a message for everyone");
            }

            if (message.getCreatedAt().plus(DELETE_FOR_EVERYONE_LIMIT).isBefore(Instant.now())) {
                throw new ApiException(ErrorCode.MESSAGE_DELETE_FORBIDDEN, "Delete for everyone window has expired (48 hours)");
            }

            message.setDeletedAt(Instant.now());
            message.setDeleteScope(DeleteScope.FOR_EVERYONE);
            message.setContent(null);
            messageRepository.save(message);

            RealtimeEvent event = RealtimeEvent.of(
                    "message.deleted",
                    message.getConversationId(),
                    Map.of("messageId", messageId, "scope", "FOR_EVERYONE")
            );
            realtimePublisher.recordOutboxEvent("message.deleted", "CONVERSATION", message.getConversationId(), event);
            realtimePublisher.publishToConversation(message.getConversationId(), event);
        }
    }
}
