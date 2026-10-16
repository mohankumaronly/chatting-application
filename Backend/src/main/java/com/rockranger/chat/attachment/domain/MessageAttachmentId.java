package com.rockranger.chat.attachment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MessageAttachmentId implements Serializable {

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "attachment_id", nullable = false)
    private UUID attachmentId;

    public MessageAttachmentId() {}

    public MessageAttachmentId(UUID messageId, UUID attachmentId) {
        this.messageId = messageId;
        this.attachmentId = attachmentId;
    }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }
    public UUID getAttachmentId() { return attachmentId; }
    public void setAttachmentId(UUID attachmentId) { this.attachmentId = attachmentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageAttachmentId that)) return false;
        return Objects.equals(messageId, that.messageId) && Objects.equals(attachmentId, that.attachmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, attachmentId);
    }
}
