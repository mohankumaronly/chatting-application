package com.rockranger.chat.attachment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttachmentReferenceRequest(
        @NotNull(message = "attachmentId is required")
        UUID attachmentId
) {}
