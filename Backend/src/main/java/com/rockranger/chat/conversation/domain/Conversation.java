package com.rockranger.chat.conversation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "direct_pair_key", length = 80)
    private String directPairKey;

    @Column(name = "next_sequence", nullable = false)
    private long nextSequence = 0L;

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Conversation() {}

    public Conversation(UUID id, ConversationType type, UUID createdBy, String directPairKey) {
        this.id = id;
        this.type = type;
        this.createdBy = createdBy;
        this.directPairKey = directPairKey;
        this.nextSequence = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static String calculateDirectPairKey(UUID a, UUID b) {
        return a.compareTo(b) < 0
                ? a.toString() + ":" + b.toString()
                : b.toString() + ":" + a.toString();
    }

    public long nextSequence() {
        this.nextSequence += 1;
        return this.nextSequence;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ConversationType getType() { return type; }
    public void setType(ConversationType type) { this.type = type; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public String getDirectPairKey() { return directPairKey; }
    public void setDirectPairKey(String directPairKey) { this.directPairKey = directPairKey; }
    public long getNextSequence() { return nextSequence; }
    public void setNextSequence(long nextSequence) { this.nextSequence = nextSequence; }
    public UUID getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(UUID lastMessageId) { this.lastMessageId = lastMessageId; }
    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
