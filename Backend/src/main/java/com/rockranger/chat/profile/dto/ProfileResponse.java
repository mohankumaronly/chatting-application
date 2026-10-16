package com.rockranger.chat.profile.dto;

import java.time.Instant;
import java.util.UUID;

public class ProfileResponse {

    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String avatarUrl;
    private boolean profileComplete;
    private Instant usernameChangedAt;

    public ProfileResponse() {}

    public ProfileResponse(UUID userId, String username, String firstName, String lastName,
                           String email, String bio, String avatarUrl,
                           boolean profileComplete, Instant usernameChangedAt) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.profileComplete = profileComplete;
        this.usernameChangedAt = usernameChangedAt;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }
    public Instant getUsernameChangedAt() { return usernameChangedAt; }
    public void setUsernameChangedAt(Instant usernameChangedAt) { this.usernameChangedAt = usernameChangedAt; }
}