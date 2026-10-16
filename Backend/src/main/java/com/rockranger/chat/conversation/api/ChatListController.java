package com.rockranger.chat.conversation.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.conversation.application.ChatListService;
import com.rockranger.chat.conversation.dto.ChatListResponse;
import com.rockranger.chat.conversation.dto.UpdateChatSettingsRequest;
import com.rockranger.chat.receipt.application.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatListController {

    private final ChatListService chatListService;
    private final ReceiptService receiptService;

    public ChatListController(ChatListService chatListService, ReceiptService receiptService) {
        this.chatListService = chatListService;
        this.receiptService = receiptService;
    }

    @GetMapping
    public ResponseEntity<ChatListResponse> getChatList(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(name = "archived", required = false) Boolean archived,
            @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        ChatListResponse response = chatListService.getChatList(currentUser.getUserId(), archived, limit);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{conversationId}/settings")
    public ResponseEntity<Map<String, String>> updateSettings(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId,
            @RequestBody UpdateChatSettingsRequest request
    ) {
        chatListService.updateSettings(currentUser.getUserId(), conversationId, request);
        return ResponseEntity.ok(Map.of("message", "Chat settings updated successfully"));
    }

    @PostMapping("/{conversationId}/archive")
    public ResponseEntity<Map<String, String>> archiveChat(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId
    ) {
        chatListService.setArchive(currentUser.getUserId(), conversationId, true);
        return ResponseEntity.ok(Map.of("message", "Conversation archived"));
    }

    @DeleteMapping("/{conversationId}/archive")
    public ResponseEntity<Map<String, String>> unarchiveChat(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId
    ) {
        chatListService.setArchive(currentUser.getUserId(), conversationId, false);
        return ResponseEntity.ok(Map.of("message", "Conversation unarchived"));
    }

    @PostMapping("/{conversationId}/hide")
    public ResponseEntity<Map<String, String>> hideChat(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId
    ) {
        chatListService.setHide(currentUser.getUserId(), conversationId, true);
        return ResponseEntity.ok(Map.of("message", "Conversation hidden"));
    }

    @DeleteMapping("/{conversationId}/hide")
    public ResponseEntity<Map<String, String>> unhideChat(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId
    ) {
        chatListService.setHide(currentUser.getUserId(), conversationId, false);
        return ResponseEntity.ok(Map.of("message", "Conversation restored"));
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<Map<String, String>> markRead(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId
    ) {
        receiptService.markRead(currentUser.getUserId(), conversationId, null);
        return ResponseEntity.ok(Map.of("message", "Conversation marked as read"));
    }
}
