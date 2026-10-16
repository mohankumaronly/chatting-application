package com.rockranger.chat.messaging.application;

import com.rockranger.chat.attachment.domain.Attachment;
import com.rockranger.chat.attachment.domain.AttachmentRepository;
import com.rockranger.chat.attachment.domain.MessageAttachment;
import com.rockranger.chat.attachment.domain.MessageAttachmentRepository;
import com.rockranger.chat.attachment.dto.AttachmentReferenceRequest;
import com.rockranger.chat.attachment.dto.AttachmentResponse;
import com.rockranger.chat.attachment.infrastructure.ObjectStorageService;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.conversation.domain.*;
import com.rockranger.chat.messaging.domain.*;
import com.rockranger.chat.messaging.dto.MessageResponse;
import com.rockranger.chat.messaging.dto.ReplyPreviewResponse;
import com.rockranger.chat.messaging.dto.SendMessageRequest;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import com.rockranger.chat.realtime.publisher.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final AttachmentRepository attachmentRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationUserStateRepository userStateRepository;
    private final ConversationAuthorizationService authorizationService;
    private final ObjectStorageService objectStorageService;
    private final RealtimePublisher realtimePublisher;
    private final RateLimitService rateLimitService;

    public MessageService(
            MessageRepository messageRepository,
            MessageAttachmentRepository messageAttachmentRepository,
            AttachmentRepository attachmentRepository,
            ConversationRepository conversationRepository,
            ConversationMemberRepository memberRepository,
            ConversationUserStateRepository userStateRepository,
            ConversationAuthorizationService authorizationService,
            ObjectStorageService objectStorageService,
            RealtimePublisher realtimePublisher,
            RateLimitService rateLimitService
    ) {
        this.messageRepository = messageRepository;
        this.messageAttachmentRepository = messageAttachmentRepository;
        this.attachmentRepository = attachmentRepository;
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.userStateRepository = userStateRepository;
        this.authorizationService = authorizationService;
        this.objectStorageService = objectStorageService;
        this.realtimePublisher = realtimePublisher;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public MessageResponse sendMessage(UUID senderId, UUID conversationId, SendMessageRequest req) {
        // Idempotency check first
        Optional<Message> existing = messageRepository.findBySenderIdAndClientMessageId(senderId, req.clientMessageId());
        if (existing.isPresent()) {
            return buildResponse(existing.get());
        }

        // Rate limit
        rateLimitService.enforce("chat:rl:message-send:" + senderId, 120, Duration.ofMinutes(1));
        rateLimitService.enforce("chat:rl:message-send:" + conversationId, 600, Duration.ofMinutes(1));

        // Authorization (membership, account status, blocks)
        authorizationService.requireCanSend(senderId, conversationId);

        // Validation based on message type
        if (req.type() == MessageType.TEXT) {
            if (req.content() == null || req.content().isBlank()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Content is required for text messages");
            }
            if (req.content().length() > 4000) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Message content exceeds limit of 4000 characters");
            }
        }

        // Validate reply target if present
        if (req.replyToMessageId() != null) {
            Message replyTarget = messageRepository.findById(req.replyToMessageId())
                    .orElseThrow(() -> new ApiException(ErrorCode.REPLY_TARGET_INVALID, "Reply target message not found"));

            if (!replyTarget.getConversationId().equals(conversationId)) {
                throw new ApiException(ErrorCode.REPLY_TARGET_INVALID, "Reply target must belong to the same conversation");
            }
        }

        // Validate attachments if present
        List<Attachment> verifiedAttachments = new ArrayList<>();
        if (req.attachments() != null && !req.attachments().isEmpty()) {
            if (req.attachments().size() > 10) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Cannot attach more than 10 files per message");
            }
            for (AttachmentReferenceRequest attRef : req.attachments()) {
                Attachment att = attachmentRepository.findById(attRef.attachmentId())
                        .orElseThrow(() -> new ApiException(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found: " + attRef.attachmentId()));

                if (!att.getOwnerId().equals(senderId)) {
                    throw new ApiException(ErrorCode.FORBIDDEN, "Cannot attach media owned by another user");
                }
                if (!att.isReady()) {
                    throw new ApiException(ErrorCode.ATTACHMENT_NOT_READY, "Attachment is not ready: " + attRef.attachmentId());
                }
                verifiedAttachments.add(att);
            }
        }

        // Pessimistic lock on conversation to generate next atomic sequence number
        Conversation conversation = conversationRepository.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found"));

        long sequenceNumber = conversation.nextSequence();

        UUID messageId = UUID.randomUUID();
        Message message = new Message(
                messageId,
                conversationId,
                senderId,
                req.clientMessageId(),
                sequenceNumber,
                req.type(),
                req.content(),
                req.replyToMessageId()
        );
        messageRepository.save(message);

        // Save attachments mapping
        int order = 0;
        for (Attachment att : verifiedAttachments) {
            messageAttachmentRepository.save(new MessageAttachment(messageId, att.getId(), order++));
        }

        // Update conversation summary
        conversation.setLastMessageId(messageId);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        // Auto-unhide conversation for all active members
        List<ConversationMember> members = memberRepository.findByConversationId(conversationId);
        for (ConversationMember member : members) {
            userStateRepository.findByConversationIdAndUserId(conversationId, member.getUserId()).ifPresent(state -> {
                if (state.getHiddenAt() != null) {
                    state.setHiddenAt(null);
                    state.setUpdatedAt(Instant.now());
                    userStateRepository.save(state);
                }
            });
        }

        MessageResponse response = buildResponse(message, verifiedAttachments);

        // Publish realtime event and record in outbox
        RealtimeEvent event = RealtimeEvent.of("message.created", conversationId, response);
        realtimePublisher.recordOutboxEvent("message.created", "CONVERSATION", conversationId, event);
        realtimePublisher.publishToConversation(conversationId, event);

        return response;
    }

    public MessageResponse buildResponse(Message message) {
        List<MessageAttachment> msgAtts = messageAttachmentRepository.findByMessageId(message.getId());
        List<Attachment> atts = msgAtts.stream()
                .map(ma -> attachmentRepository.findById(ma.getAttachmentId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        return buildResponse(message, atts);
    }

    public MessageResponse buildResponse(Message message, List<Attachment> attachments) {
        ReplyPreviewResponse replyPreview = null;
        if (message.getReplyToMessageId() != null) {
            Optional<Message> replyTarget = messageRepository.findById(message.getReplyToMessageId());
            if (replyTarget.isPresent()) {
                Message target = replyTarget.get();
                String preview = target.isDeleted() ? "This message was deleted" : target.getContent();
                replyPreview = new ReplyPreviewResponse(
                        target.getId(),
                        target.getSenderId(),
                        target.getMessageType().name(),
                        preview
                );
            }
        }

        List<AttachmentResponse> attachmentResponses = attachments.stream()
                .map(att -> {
                    String downloadUrl = objectStorageService.createSignedDownloadUrl(att.getStorageKey(), Duration.ofHours(1));
                    return new AttachmentResponse(
                            att.getId(),
                            att.getOwnerId(),
                            att.getResourceType().name(),
                            att.getMimeType(),
                            att.getSizeBytes(),
                            att.getOriginalFilename(),
                            att.getWidth(),
                            att.getHeight(),
                            att.getDurationMs(),
                            att.getUploadStatus().name(),
                            downloadUrl,
                            att.getCreatedAt()
                    );
                })
                .toList();

        String content = message.isDeleted() ? "This message was deleted" : message.getContent();

        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getClientMessageId(),
                message.getMessageType().name(),
                content,
                message.getSequenceNumber(),
                replyPreview,
                attachmentResponses,
                message.getCreatedAt(),
                message.getEditedAt(),
                message.isDeleted()
        );
    }
}
