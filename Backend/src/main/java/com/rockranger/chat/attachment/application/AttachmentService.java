package com.rockranger.chat.attachment.application;

import com.rockranger.chat.attachment.domain.*;
import com.rockranger.chat.attachment.dto.AttachmentResponse;
import com.rockranger.chat.attachment.dto.UploadIntentRequest;
import com.rockranger.chat.attachment.dto.UploadIntentResponse;
import com.rockranger.chat.attachment.infrastructure.AttachmentMetadata;
import com.rockranger.chat.attachment.infrastructure.ObjectStorageService;
import com.rockranger.chat.attachment.infrastructure.UploadIntent;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.domain.ConversationMemberRepository;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageRepository;
import com.rockranger.chat.rate_limit.RateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    public static final long ATTACHMENT_IMAGE_MAX_BYTES = 10 * 1024 * 1024L;     // 10 MB
    public static final long ATTACHMENT_GIF_MAX_BYTES = 5 * 1024 * 1024L;        // 5 MB
    public static final long ATTACHMENT_VIDEO_MAX_BYTES = 100 * 1024 * 1024L;    // 100 MB
    public static final long ATTACHMENT_DOCUMENT_MAX_BYTES = 25 * 1024 * 1024L;  // 25 MB
    public static final long ATTACHMENT_AUDIO_MAX_BYTES = 25 * 1024 * 1024L;     // 25 MB

    private final AttachmentRepository attachmentRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final ObjectStorageService objectStorageService;
    private final RateLimitService rateLimitService;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            MessageAttachmentRepository messageAttachmentRepository,
            MessageRepository messageRepository,
            ConversationMemberRepository memberRepository,
            ObjectStorageService objectStorageService,
            RateLimitService rateLimitService
    ) {
        this.attachmentRepository = attachmentRepository;
        this.messageAttachmentRepository = messageAttachmentRepository;
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
        this.objectStorageService = objectStorageService;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public UploadIntentResponse createUploadIntent(UUID currentUserId, UploadIntentRequest req) {
        rateLimitService.enforce("chat:rl:upload-intent:" + currentUserId, 30, Duration.ofHours(1));

        ResourceType resourceType = determineResourceType(req.mimeType());
        validateSize(resourceType, req.sizeBytes(), req.mimeType());

        UUID attachmentId = UUID.randomUUID();
        UploadIntent intent = objectStorageService.createUploadIntent(
                req.filename(), req.mimeType(), req.sizeBytes(), currentUserId
        );

        String folder = (String) intent.uploadFields().get("folder");
        String publicId = (String) intent.uploadFields().get("public_id");
        String fullStorageKey = folder + "/" + publicId;

        Attachment attachment = new Attachment(
                attachmentId,
                currentUserId,
                fullStorageKey,
                resourceType,
                req.mimeType(),
                req.sizeBytes(),
                req.filename()
        );
        attachmentRepository.save(attachment);

        return new UploadIntentResponse(
                attachmentId,
                "POST",
                intent.uploadUrl(),
                intent.uploadFields(),
                intent.expiresAt(),
                UploadStatus.PENDING.name()
        );
    }

    @Transactional
    public AttachmentResponse completeUpload(UUID currentUserId, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));

        if (!attachment.getOwnerId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "You do not own this attachment");
        }

        AttachmentMetadata metadata = objectStorageService.verifyUpload(attachment.getStorageKey(), currentUserId);

        attachment.setUploadStatus(UploadStatus.READY);
        if (metadata.bytes() > 0) {
            attachment.setSizeBytes(metadata.bytes());
        }
        attachment.setWidth(metadata.width());
        attachment.setHeight(metadata.height());
        attachment.setDurationMs(metadata.durationMs());

        attachmentRepository.save(attachment);

        String downloadUrl = objectStorageService.createSignedDownloadUrl(attachment.getStorageKey(), Duration.ofHours(1));

        return toResponse(attachment, downloadUrl);
    }

    @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(UUID currentUserId, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));

        boolean authorized = attachment.getOwnerId().equals(currentUserId);

        if (!authorized) {
            // Check if user is a member of any conversation containing a message with this attachment
            List<MessageAttachment> msgAttachments = messageAttachmentRepository.findByAttachmentId(attachmentId);
            for (MessageAttachment ma : msgAttachments) {
                Message msg = messageRepository.findById(ma.getMessageId()).orElse(null);
                if (msg != null && memberRepository.existsActive(msg.getConversationId(), currentUserId)) {
                    authorized = true;
                    break;
                }
            }
        }

        if (!authorized) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Access to attachment denied");
        }

        rateLimitService.enforce("chat:rl:attachment-download:" + currentUserId, 300, Duration.ofHours(1));

        String downloadUrl = objectStorageService.createSignedDownloadUrl(attachment.getStorageKey(), Duration.ofHours(1));
        return toResponse(attachment, downloadUrl);
    }

    @Transactional
    public void deleteAttachment(UUID currentUserId, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));

        if (!attachment.getOwnerId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "You do not own this attachment");
        }

        objectStorageService.deleteAsset(attachment.getStorageKey());
        attachmentRepository.delete(attachment);
    }

    @Scheduled(fixedRate = 3600000) // hourly
    @Transactional
    public void cleanupOrphanedUploads() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        List<Attachment> orphaned = attachmentRepository.findByUploadStatusAndCreatedAtBefore(UploadStatus.PENDING, cutoff);

        for (Attachment att : orphaned) {
            try {
                objectStorageService.deleteAsset(att.getStorageKey());
                att.setUploadStatus(UploadStatus.REJECTED);
                attachmentRepository.save(att);
            } catch (Exception e) {
                log.warn("Error cleaning up orphaned attachment {}", att.getId(), e);
            }
        }
    }

    private ResourceType determineResourceType(String mimeType) {
        String lower = mimeType.toLowerCase();
        if (lower.startsWith("image/")) {
            return ResourceType.IMAGE;
        } else if (lower.startsWith("video/")) {
            return ResourceType.VIDEO;
        } else if (lower.startsWith("audio/")) {
            return ResourceType.AUDIO;
        } else {
            return ResourceType.RAW;
        }
    }

    private void validateSize(ResourceType type, long sizeBytes, String mimeType) {
        long limit;
        if ("image/gif".equalsIgnoreCase(mimeType)) {
            limit = ATTACHMENT_GIF_MAX_BYTES;
        } else {
            limit = switch (type) {
                case IMAGE -> ATTACHMENT_IMAGE_MAX_BYTES;
                case VIDEO -> ATTACHMENT_VIDEO_MAX_BYTES;
                case AUDIO -> ATTACHMENT_AUDIO_MAX_BYTES;
                case RAW -> ATTACHMENT_DOCUMENT_MAX_BYTES;
            };
        }

        if (sizeBytes > limit) {
            throw new ApiException(ErrorCode.ATTACHMENT_TOO_LARGE,
                    "File size " + sizeBytes + " exceeds limit " + limit + " for " + type);
        }
    }

    private AttachmentResponse toResponse(Attachment a, String downloadUrl) {
        return new AttachmentResponse(
                a.getId(),
                a.getOwnerId(),
                a.getResourceType().name(),
                a.getMimeType(),
                a.getSizeBytes(),
                a.getOriginalFilename(),
                a.getWidth(),
                a.getHeight(),
                a.getDurationMs(),
                a.getUploadStatus().name(),
                downloadUrl,
                a.getCreatedAt()
        );
    }
}
