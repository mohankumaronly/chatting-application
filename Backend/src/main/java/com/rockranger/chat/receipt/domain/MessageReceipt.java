package com.rockranger.chat.receipt.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_receipts")
public class MessageReceipt {

    @EmbeddedId
    private MessageReceiptId id;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    public MessageReceipt() {}

    public MessageReceipt(UUID messageId, UUID userId) {
        this.id = new MessageReceiptId(messageId, userId);
    }

    public MessageReceipt(UUID messageId, UUID userId, Instant deliveredAt, Instant readAt) {
        this.id = new MessageReceiptId(messageId, userId);
        this.deliveredAt = deliveredAt;
        this.readAt = readAt;
    }

    public UUID getMessageId() { return id != null ? id.getMessageId() : null; }
    public UUID getUserId() { return id != null ? id.getUserId() : null; }

    public MessageReceiptId getId() { return id; }
    public void setId(MessageReceiptId id) { this.id = id; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
