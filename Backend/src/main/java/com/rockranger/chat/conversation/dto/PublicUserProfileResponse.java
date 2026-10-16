package com.rockranger.chat.conversation.dto;

import java.util.UUID;

public record PublicUserProfileResponse(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        String bio
) {}
