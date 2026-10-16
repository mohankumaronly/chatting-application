package com.rockranger.chat.receipt.dto;

import java.util.UUID;

public record ReceiptCursorRequest(
        UUID throughMessageId,
        Long throughSequenceNumber
) {}
