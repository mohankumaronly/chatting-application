package com.rockranger.chat.messaging.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "client_message_id", nullable = false)
    private UUID clientMessageId;

    @Column(name = "sequence_number", nullable = false)
    private long sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "reply_to_message_id")
    private UUID replyToMessageId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delete_scope", length = 20)
    private DeleteScope deleteScope;

    /**
     * Stored as PostgreSQL {@code jsonb}.
     *
     * <p>{@code @JdbcTypeCode(SqlTypes.JSON)} is required so Hibernate binds this
     * parameter as JDBC {@code OTHER} (mapped by the PostgreSQL driver to
     * {@code jsonb}/{@code json}) instead of {@code VARCHAR}. Without it,
     * PostgreSQL rejects inserts with:
     * "column \"metadata\" is of type jsonb but expression is of type character varying".
     * {@code columnDefinition = "JSONB"} only affects DDL generation and is
     * irrelevant at runtime because {@code ddl-auto: validate} is used.</p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    public Message() {}

    public Message(UUID id, UUID conversationId, UUID senderId, UUID clientMessageId,
                   long sequenceNumber, MessageType messageType, String content, UUID replyToMessageId) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.clientMessageId = clientMessageId;
        this.sequenceNumber = sequenceNumber;
        this.messageType = messageType;
        this.content = content;
        this.replyToMessageId = replyToMessageId;
        this.createdAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }
    public UUID getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(UUID clientMessageId) { this.clientMessageId = clientMessageId; }
    public long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public UUID getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(UUID replyToMessageId) { this.replyToMessageId = replyToMessageId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getEditedAt() { return editedAt; }
    public void setEditedAt(Instant editedAt) { this.editedAt = editedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public DeleteScope getDeleteScope() { return deleteScope; }
    public void setDeleteScope(DeleteScope deleteScope) { this.deleteScope = deleteScope; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}