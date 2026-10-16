package com.rockranger.chat.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateAvatarRequest {

    @NotBlank(message = "Avatar public ID is required")
    @Size(max = 255, message = "Avatar public ID too long")
    @Pattern(
            regexp = "^chat-app/avatars/[a-zA-Z0-9_-]+$",
            message = "Avatar public ID must be in the form chat-app/avatars/{userId}"
    )
    private String avatarPublicId;

    @NotBlank(message = "Avatar URL is required")
    @Size(max = 500, message = "Avatar URL too long")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/[a-zA-Z0-9_-]+/image/upload/.+$",
            message = "Avatar URL must be a valid Cloudinary URL"
    )
    private String avatarUrl;

    public String getAvatarPublicId() { return avatarPublicId; }
    public void setAvatarPublicId(String avatarPublicId) { this.avatarPublicId = avatarPublicId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}