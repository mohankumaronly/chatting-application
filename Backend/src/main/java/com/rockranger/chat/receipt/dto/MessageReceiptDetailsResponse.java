package com.rockranger.chat.receipt.dto;

import java.util.List;
import java.util.UUID;

public record MessageReceiptDetailsResponse(
        UUID messageId,
        List<MessageReceiptResponse> receipts
) {}
