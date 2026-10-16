package com.rockranger.chat.receipt.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageReceiptResponse(
        UUID messageId,
        UUID userId,
        Instant deliveredAt,
        Instant readAt
) {}
