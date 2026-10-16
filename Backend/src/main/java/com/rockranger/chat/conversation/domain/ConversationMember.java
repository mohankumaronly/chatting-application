package com.rockranger.chat.conversation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
public class ConversationMember {

    @EmbeddedId
    private ConversationMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationMemberRole role = ConversationMemberRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    public ConversationMember() {}

    public ConversationMember(UUID conversationId, UUID userId, ConversationMemberRole role) {
        this.id = new ConversationMemberId(conversationId, userId);
        this.role = role != null ? role : ConversationMemberRole.MEMBER;
        this.joinedAt = Instant.now();
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public UUID getConversationId() {
        return id != null ? id.getConversationId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public ConversationMemberId getId() { return id; }
    public void setId(ConversationMemberId id) { this.id = id; }
    public ConversationMemberRole getRole() { return role; }
    public void setRole(ConversationMemberRole role) { this.role = role; }
    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
    public Instant getLeftAt() { return leftAt; }
    public void setLeftAt(Instant leftAt) { this.leftAt = leftAt; }
    public UUID getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(UUID lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }
    public Instant getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Instant lastReadAt) { this.lastReadAt = lastReadAt; }
}
