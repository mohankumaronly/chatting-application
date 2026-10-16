package com.rockranger.chat.messaging.application;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageRepository;
import com.rockranger.chat.messaging.domain.MessageUserDeletionRepository;
import com.rockranger.chat.messaging.dto.MessageHistoryResponse;
import com.rockranger.chat.messaging.dto.MessageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MessageQueryService {

    private final MessageRepository messageRepository;
    private final MessageUserDeletionRepository userDeletionRepository;
    private final ConversationAuthorizationService authorizationService;
    private final MessageService messageService;

    public MessageQueryService(
            MessageRepository messageRepository,
            MessageUserDeletionRepository userDeletionRepository,
            ConversationAuthorizationService authorizationService,
            MessageService messageService
    ) {
        this.messageRepository = messageRepository;
        this.userDeletionRepository = userDeletionRepository;
        this.authorizationService = authorizationService;
        this.messageService = messageService;
    }

    @Transactional(readOnly = true)
    public MessageHistoryResponse getHistory(UUID currentUserId, UUID conversationId, Long beforeSequence, int limit) {
        authorizationService.requireCanView(currentUserId, conversationId);

        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        // Fetch one extra item to determine hasMore accurately
        PageRequest pageRequest = PageRequest.of(0, boundedLimit + 1);

        List<Message> rawMessages;
        if (beforeSequence != null) {
            rawMessages = messageRepository.findByConversationIdBeforeSequence(conversationId, beforeSequence, pageRequest);
        } else {
            rawMessages = messageRepository.findLatestByConversationId(conversationId, pageRequest);
        }

        boolean hasMore = rawMessages.size() > boundedLimit;
        List<Message> pageMessages = hasMore ? rawMessages.subList(0, boundedLimit) : rawMessages;

        List<UUID> messageIds = pageMessages.stream().map(Message::getId).toList();
        Set<UUID> deletedForMeIds = userDeletionRepository.findDeletedMessageIdsForUser(currentUserId, messageIds);

        List<MessageResponse> responses = pageMessages.stream()
                .filter(m -> !deletedForMeIds.contains(m.getId()))
                .map(messageService::buildResponse)
                .toList();

        Long nextCursor = null;
        if (!pageMessages.isEmpty()) {
            nextCursor = pageMessages.get(pageMessages.size() - 1).getSequenceNumber();
        }

        return new MessageHistoryResponse(responses, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessage(UUID currentUserId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        authorizationService.requireCanView(currentUserId, message.getConversationId());

        if (userDeletionRepository.isMessageDeletedForUser(messageId, currentUserId)) {
            throw new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found");
        }

        return messageService.buildResponse(message);
    }
}
