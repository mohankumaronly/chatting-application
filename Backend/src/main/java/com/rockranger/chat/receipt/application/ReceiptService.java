package com.rockranger.chat.receipt.application;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.conversation.domain.ConversationMember;
import com.rockranger.chat.conversation.domain.ConversationMemberRepository;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageRepository;
import com.rockranger.chat.presence.domain.UserPresenceSettings;
import com.rockranger.chat.presence.domain.UserPresenceSettingsRepository;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import com.rockranger.chat.realtime.publisher.RealtimePublisher;
import com.rockranger.chat.receipt.domain.MessageReceipt;
import com.rockranger.chat.receipt.domain.MessageReceiptRepository;
import com.rockranger.chat.receipt.dto.MessageReceiptDetailsResponse;
import com.rockranger.chat.receipt.dto.MessageReceiptResponse;
import com.rockranger.chat.receipt.dto.ReceiptCursorRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ReceiptService {

    private final MessageReceiptRepository receiptRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationAuthorizationService authorizationService;
    private final UserPresenceSettingsRepository presenceSettingsRepository;
    private final RealtimePublisher realtimePublisher;

    public ReceiptService(
            MessageReceiptRepository receiptRepository,
            MessageRepository messageRepository,
            ConversationMemberRepository memberRepository,
            ConversationAuthorizationService authorizationService,
            UserPresenceSettingsRepository presenceSettingsRepository,
            RealtimePublisher realtimePublisher
    ) {
        this.receiptRepository = receiptRepository;
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
        this.authorizationService = authorizationService;
        this.presenceSettingsRepository = presenceSettingsRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public void markDelivered(UUID currentUserId, UUID conversationId, ReceiptCursorRequest req) {
        authorizationService.requireCanView(currentUserId, conversationId);

        long maxSequence = resolveSequence(conversationId, req);

        List<Message> messages = messageRepository.findLatestByConversationId(conversationId, PageRequest.of(0, 100));
        Instant now = Instant.now();

        for (Message msg : messages) {
            if (msg.getSequenceNumber() <= maxSequence && !msg.getSenderId().equals(currentUserId)) {
                MessageReceipt receipt = receiptRepository
                        .findByMessageIdAndUserId(msg.getId(), currentUserId)
                        .orElseGet(() -> new MessageReceipt(msg.getId(), currentUserId));

                if (receipt.getDeliveredAt() == null) {
                    receipt.setDeliveredAt(now);
                    receiptRepository.save(receipt);
                }
            }
        }

        RealtimeEvent event = RealtimeEvent.of(
                "message.delivered",
                conversationId,
                Map.of("conversationId", conversationId, "userId", currentUserId, "throughSequence", maxSequence)
        );
        realtimePublisher.publishToConversation(conversationId, event);
    }

    @Transactional
    public void markRead(UUID currentUserId, UUID conversationId, ReceiptCursorRequest req) {
        authorizationService.requireCanView(currentUserId, conversationId);

        long maxSequence = resolveSequence(conversationId, req);
        UUID lastMsgId = req != null && req.throughMessageId() != null ? req.throughMessageId() : null;

        Instant now = Instant.now();

        // Update member's read cursor
        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_ACCESS_DENIED, "Membership not found"));

        member.setLastReadAt(now);
        if (lastMsgId != null) {
            member.setLastReadMessageId(lastMsgId);
        }
        memberRepository.save(member);

        // Update receipt rows
        List<Message> messages = messageRepository.findLatestByConversationId(conversationId, PageRequest.of(0, 100));
        for (Message msg : messages) {
            if (msg.getSequenceNumber() <= maxSequence && !msg.getSenderId().equals(currentUserId)) {
                MessageReceipt receipt = receiptRepository
                        .findByMessageIdAndUserId(msg.getId(), currentUserId)
                        .orElseGet(() -> new MessageReceipt(msg.getId(), currentUserId));

                if (receipt.getDeliveredAt() == null) {
                    receipt.setDeliveredAt(now);
                }
                receipt.setReadAt(now);
                receiptRepository.save(receipt);
            }
        }

        // Privacy check for read receipts
        UserPresenceSettings settings = presenceSettingsRepository.findById(currentUserId)
                .orElseGet(() -> UserPresenceSettings.defaults(currentUserId));

        if (settings.isReadReceiptsEnabled()) {
            RealtimeEvent event = RealtimeEvent.of(
                    "message.read",
                    conversationId,
                    Map.of("conversationId", conversationId, "userId", currentUserId, "throughSequence", maxSequence)
            );
            realtimePublisher.recordOutboxEvent("message.read", "CONVERSATION", conversationId, event);
            realtimePublisher.publishToConversation(conversationId, event);
        }
    }

    @Transactional(readOnly = true)
    public MessageReceiptDetailsResponse getMessageReceipts(UUID currentUserId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));

        authorizationService.requireCanView(currentUserId, message.getConversationId());

        List<MessageReceipt> receipts = receiptRepository.findByMessageId(messageId);
        List<MessageReceiptResponse> responses = receipts.stream()
                .map(r -> new MessageReceiptResponse(r.getMessageId(), r.getUserId(), r.getDeliveredAt(), r.getReadAt()))
                .toList();

        return new MessageReceiptDetailsResponse(messageId, responses);
    }

    private long resolveSequence(UUID conversationId, ReceiptCursorRequest req) {
        if (req != null && req.throughSequenceNumber() != null) {
            return req.throughSequenceNumber();
        }
        if (req != null && req.throughMessageId() != null) {
            return messageRepository.findById(req.throughMessageId())
                    .map(Message::getSequenceNumber)
                    .orElse(Long.MAX_VALUE);
        }
        return messageRepository.findFirstByConversationIdOrderBySequenceNumberDesc(conversationId)
                .map(Message::getSequenceNumber)
                .orElse(0L);
    }
}
