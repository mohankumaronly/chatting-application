package com.rockranger.chat.reaction.application;

import com.rockranger.chat.block.application.BlockPolicy;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.conversation.domain.ConversationMemberRepository;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageRepository;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.reaction.domain.MessageReaction;
import com.rockranger.chat.reaction.domain.MessageReactionRepository;
import com.rockranger.chat.reaction.dto.AddReactionRequest;
import com.rockranger.chat.reaction.dto.MessageReactionsResponse;
import com.rockranger.chat.reaction.dto.ReactionGroupResponse;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import com.rockranger.chat.realtime.publisher.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageReactionService {

    private final MessageReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationAuthorizationService authorizationService;
    private final BlockPolicy blockPolicy;
    private final RealtimePublisher realtimePublisher;
    private final RateLimitService rateLimitService;

    public MessageReactionService(
            MessageReactionRepository reactionRepository,
            MessageRepository messageRepository,
            ConversationMemberRepository memberRepository,
            ConversationAuthorizationService authorizationService,
            BlockPolicy blockPolicy,
            RealtimePublisher realtimePublisher,
            RateLimitService rateLimitService
    ) {
        this.reactionRepository = reactionRepository;
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
        this.authorizationService = authorizationService;
        this.blockPolicy = blockPolicy;
        this.realtimePublisher = realtimePublisher;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public MessageReactionsResponse addReaction(UUID currentUserId, UUID messageId, AddReactionRequest req) {
        rateLimitService.enforce("chat:rl:reaction:" + currentUserId, 120, Duration.ofMinutes(1));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        if (message.isDeleted()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot react to a deleted message");
        }

        authorizationService.requireCanView(currentUserId, message.getConversationId());

        Set<UUID> otherMembers = memberRepository.findOtherActiveMemberIds(message.getConversationId(), currentUserId);
        for (UUID otherId : otherMembers) {
            if (blockPolicy.hasAnyBlock(currentUserId, otherId)) {
                throw new ApiException(ErrorCode.USER_BLOCKED, "Cannot react in conversation with blocked user");
            }
        }

        Optional<MessageReaction> existing = reactionRepository
                .findByMessageIdAndUserIdAndEmoji(messageId, currentUserId, req.emoji());

        if (existing.isEmpty()) {
            MessageReaction reaction = new MessageReaction(UUID.randomUUID(), messageId, currentUserId, req.emoji());
            reactionRepository.save(reaction);

            RealtimeEvent event = RealtimeEvent.of(
                    "message.reaction.added",
                    message.getConversationId(),
                    Map.of("messageId", messageId, "userId", currentUserId, "emoji", req.emoji())
            );
            realtimePublisher.recordOutboxEvent("message.reaction.added", "CONVERSATION", message.getConversationId(), event);
            realtimePublisher.publishToConversation(message.getConversationId(), event);
        }

        return getReactions(currentUserId, messageId);
    }

    @Transactional
    public MessageReactionsResponse removeReaction(UUID currentUserId, UUID messageId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        authorizationService.requireCanView(currentUserId, message.getConversationId());

        reactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, currentUserId, emoji)
                .ifPresent(reaction -> {
                    reactionRepository.delete(reaction);

                    RealtimeEvent event = RealtimeEvent.of(
                            "message.reaction.removed",
                            message.getConversationId(),
                            Map.of("messageId", messageId, "userId", currentUserId, "emoji", emoji)
                    );
                    realtimePublisher.recordOutboxEvent("message.reaction.removed", "CONVERSATION", message.getConversationId(), event);
                    realtimePublisher.publishToConversation(message.getConversationId(), event);
                });

        return getReactions(currentUserId, messageId);
    }

    @Transactional(readOnly = true)
    public MessageReactionsResponse getReactions(UUID currentUserId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        authorizationService.requireCanView(currentUserId, message.getConversationId());

        List<MessageReaction> reactions = reactionRepository.findByMessageId(messageId);

        Map<String, List<MessageReaction>> grouped = reactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji));

        List<ReactionGroupResponse> groupResponses = grouped.entrySet().stream()
                .map(entry -> {
                    String emoji = entry.getKey();
                    long count = entry.getValue().size();
                    boolean reactedByMe = entry.getValue().stream().anyMatch(r -> r.getUserId().equals(currentUserId));
                    return new ReactionGroupResponse(emoji, count, reactedByMe);
                })
                .toList();

        return new MessageReactionsResponse(messageId, groupResponses);
    }
}
