package com.rockranger.chat.profile.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 30)
    private String username;

    @Column(name = "username_normalized", unique = true, length = 30)
    private String usernameNormalized;

    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_public_id", length = 255)
    private String avatarPublicId;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "username_changed_at")
    private Instant usernameChangedAt;

    @Column(name = "profile_created_at", nullable = false)
    private Instant profileCreatedAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserProfile() {}

    public UserProfile(UUID userId) {
        this.userId = userId;
        this.profileCreatedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUsernameNormalized() { return usernameNormalized; }
    public void setUsernameNormalized(String usernameNormalized) { this.usernameNormalized = usernameNormalized; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarPublicId() { return avatarPublicId; }
    public void setAvatarPublicId(String avatarPublicId) { this.avatarPublicId = avatarPublicId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Instant getUsernameChangedAt() { return usernameChangedAt; }
    public void setUsernameChangedAt(Instant usernameChangedAt) { this.usernameChangedAt = usernameChangedAt; }
    public Instant getProfileCreatedAt() { return profileCreatedAt; }
    public void setProfileCreatedAt(Instant profileCreatedAt) { this.profileCreatedAt = profileCreatedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}