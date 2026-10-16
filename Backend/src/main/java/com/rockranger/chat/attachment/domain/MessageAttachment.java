package com.rockranger.chat.attachment.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "message_attachments")
public class MessageAttachment {

    @EmbeddedId
    private MessageAttachmentId id;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    public MessageAttachment() {}

    public MessageAttachment(UUID messageId, UUID attachmentId, int displayOrder) {
        this.id = new MessageAttachmentId(messageId, attachmentId);
        this.displayOrder = displayOrder;
    }

    public UUID getMessageId() { return id != null ? id.getMessageId() : null; }
    public UUID getAttachmentId() { return id != null ? id.getAttachmentId() : null; }

    public MessageAttachmentId getId() { return id; }
    public void setId(MessageAttachmentId id) { this.id = id; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
