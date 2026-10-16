package com.rockranger.chat.conversation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_user_state")
public class ConversationUserState {

    @EmbeddedId
    private ConversationUserStateId id;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "pinned_at")
    private Instant pinnedAt;

    @Column(name = "muted_until")
    private Instant mutedUntil;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "unread_override", nullable = false)
    private boolean unreadOverride = false;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ConversationUserState() {}

    public ConversationUserState(UUID conversationId, UUID userId) {
        this.id = new ConversationUserStateId(conversationId, userId);
        this.updatedAt = Instant.now();
    }

    public UUID getConversationId() {
        return id != null ? id.getConversationId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean isPinned() {
        return pinnedAt != null;
    }

    public boolean isMuted() {
        return mutedUntil != null && mutedUntil.isAfter(Instant.now());
    }

    public boolean isHidden() {
        return hiddenAt != null;
    }

    public ConversationUserStateId getId() { return id; }
    public void setId(ConversationUserStateId id) { this.id = id; }
    public Instant getArchivedAt() { return archivedAt; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }
    public Instant getPinnedAt() { return pinnedAt; }
    public void setPinnedAt(Instant pinnedAt) { this.pinnedAt = pinnedAt; }
    public Instant getMutedUntil() { return mutedUntil; }
    public void setMutedUntil(Instant mutedUntil) { this.mutedUntil = mutedUntil; }
    public Instant getHiddenAt() { return hiddenAt; }
    public void setHiddenAt(Instant hiddenAt) { this.hiddenAt = hiddenAt; }
    public boolean isUnreadOverride() { return unreadOverride; }
    public void setUnreadOverride(boolean unreadOverride) { this.unreadOverride = unreadOverride; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
