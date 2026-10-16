package com.rockranger.chat.reaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddReactionRequest(
        @NotBlank(message = "emoji is required")
        @Size(max = 16, message = "emoji must be at most 16 characters")
        String emoji
) {}
