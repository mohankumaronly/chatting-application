package com.rockranger.chat.attachment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UploadIntentRequest(
        @NotBlank(message = "filename is required")
        String filename,

        @NotBlank(message = "mimeType is required")
        String mimeType,

        @Positive(message = "sizeBytes must be positive")
        long sizeBytes
) {}
