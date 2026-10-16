package com.rockranger.chat.presence.dto;

import com.rockranger.chat.presence.domain.VisibilitySetting;

public record UpdatePrivacySettingsRequest(
        VisibilitySetting lastSeenVisibility,
        VisibilitySetting onlineVisibility,
        Boolean readReceiptsEnabled,
        Boolean typingVisibility,
        VisibilitySetting profilePhotoVisibility
) {}
