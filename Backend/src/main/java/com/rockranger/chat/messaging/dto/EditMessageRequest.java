package com.rockranger.chat.messaging.dto;

import jakarta.validation.constraints.NotBlank;

public record EditMessageRequest(
        @NotBlank(message = "content must not be blank")
        String content
) {}
