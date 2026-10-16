package com.rockranger.chat.attachment.infrastructure;

import java.time.Instant;
import java.util.Map;

public record UploadIntent(
        String uploadUrl,
        Map<String, Object> uploadFields,
        Instant expiresAt
) {}
