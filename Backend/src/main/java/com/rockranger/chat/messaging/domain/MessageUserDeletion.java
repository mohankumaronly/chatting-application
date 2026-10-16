package com.rockranger.chat.messaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_user_deletions")
public class MessageUserDeletion {

    @EmbeddedId
    private MessageUserDeletionId id;

    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt = Instant.now();

    public MessageUserDeletion() {}

    public MessageUserDeletion(UUID messageId, UUID userId) {
        this.id = new MessageUserDeletionId(messageId, userId);
        this.deletedAt = Instant.now();
    }

    public UUID getMessageId() { return id != null ? id.getMessageId() : null; }
    public UUID getUserId() { return id != null ? id.getUserId() : null; }

    public MessageUserDeletionId getId() { return id; }
    public void setId(MessageUserDeletionId id) { this.id = id; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
