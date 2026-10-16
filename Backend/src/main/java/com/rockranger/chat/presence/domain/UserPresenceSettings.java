package com.rockranger.chat.presence.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_presence_settings")
public class UserPresenceSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_seen_visibility", nullable = false, length = 20)
    private VisibilitySetting lastSeenVisibility = VisibilitySetting.EVERYONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_visibility", nullable = false, length = 20)
    private VisibilitySetting onlineVisibility = VisibilitySetting.EVERYONE;

    @Column(name = "read_receipts_enabled", nullable = false)
    private boolean readReceiptsEnabled = true;

    @Column(name = "typing_visibility", nullable = false)
    private boolean typingVisibility = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_photo_visibility", nullable = false, length = 20)
    private VisibilitySetting profilePhotoVisibility = VisibilitySetting.EVERYONE;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserPresenceSettings() {}

    public UserPresenceSettings(UUID userId) {
        this.userId = userId;
        this.lastSeenVisibility = VisibilitySetting.EVERYONE;
        this.onlineVisibility = VisibilitySetting.EVERYONE;
        this.readReceiptsEnabled = true;
        this.typingVisibility = true;
        this.profilePhotoVisibility = VisibilitySetting.EVERYONE;
        this.updatedAt = Instant.now();
    }

    public static UserPresenceSettings defaults(UUID userId) {
        return new UserPresenceSettings(userId);
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public VisibilitySetting getLastSeenVisibility() { return lastSeenVisibility; }
    public void setLastSeenVisibility(VisibilitySetting lastSeenVisibility) { this.lastSeenVisibility = lastSeenVisibility; }
    public VisibilitySetting getOnlineVisibility() { return onlineVisibility; }
    public void setOnlineVisibility(VisibilitySetting onlineVisibility) { this.onlineVisibility = onlineVisibility; }
    public boolean isReadReceiptsEnabled() { return readReceiptsEnabled; }
    public void setReadReceiptsEnabled(boolean readReceiptsEnabled) { this.readReceiptsEnabled = readReceiptsEnabled; }
    public boolean isTypingVisibility() { return typingVisibility; }
    public void setTypingVisibility(boolean typingVisibility) { this.typingVisibility = typingVisibility; }
    public VisibilitySetting getProfilePhotoVisibility() { return profilePhotoVisibility; }
    public void setProfilePhotoVisibility(VisibilitySetting profilePhotoVisibility) { this.profilePhotoVisibility = profilePhotoVisibility; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
