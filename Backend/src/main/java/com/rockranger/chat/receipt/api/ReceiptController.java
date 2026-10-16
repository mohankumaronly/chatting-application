package com.rockranger.chat.receipt.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.receipt.application.ReceiptService;
import com.rockranger.chat.receipt.dto.MessageReceiptDetailsResponse;
import com.rockranger.chat.receipt.dto.ReceiptCursorRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping("/api/v1/conversations/{conversationId}/receipts/delivered")
    public ResponseEntity<Map<String, String>> markDelivered(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId,
            @RequestBody(required = false) ReceiptCursorRequest request
    ) {
        receiptService.markDelivered(currentUser.getUserId(), conversationId, request);
        return ResponseEntity.ok(Map.of("message", "Delivery acknowledged"));
    }

    @PostMapping("/api/v1/conversations/{conversationId}/receipts/read")
    public ResponseEntity<Map<String, String>> markRead(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId,
            @RequestBody(required = false) ReceiptCursorRequest request
    ) {
        receiptService.markRead(currentUser.getUserId(), conversationId, request);
        return ResponseEntity.ok(Map.of("message", "Read cursor advanced"));
    }

    @GetMapping("/api/v1/messages/{messageId}/receipts")
    public ResponseEntity<MessageReceiptDetailsResponse> getReceipts(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId
    ) {
        MessageReceiptDetailsResponse response = receiptService.getMessageReceipts(currentUser.getUserId(), messageId);
        return ResponseEntity.ok(response);
    }
}
