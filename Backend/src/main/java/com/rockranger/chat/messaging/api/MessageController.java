package com.rockranger.chat.messaging.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.messaging.application.MessageDeletionService;
import com.rockranger.chat.messaging.application.MessageEditService;
import com.rockranger.chat.messaging.application.MessageQueryService;
import com.rockranger.chat.messaging.application.MessageService;
import com.rockranger.chat.messaging.domain.DeleteScope;
import com.rockranger.chat.messaging.dto.EditMessageRequest;
import com.rockranger.chat.messaging.dto.MessageHistoryResponse;
import com.rockranger.chat.messaging.dto.MessageResponse;
import com.rockranger.chat.messaging.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class MessageController {

    private final MessageService messageService;
    private final MessageQueryService messageQueryService;
    private final MessageEditService messageEditService;
    private final MessageDeletionService messageDeletionService;

    public MessageController(
            MessageService messageService,
            MessageQueryService messageQueryService,
            MessageEditService messageEditService,
            MessageDeletionService messageDeletionService
    ) {
        this.messageService = messageService;
        this.messageQueryService = messageQueryService;
        this.messageEditService = messageEditService;
        this.messageDeletionService = messageDeletionService;
    }

    @PostMapping("/api/v1/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse response = messageService.sendMessage(currentUser.getUserId(), conversationId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/conversations/{conversationId}/messages")
    public ResponseEntity<MessageHistoryResponse> getHistory(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId,
            @RequestParam(name = "before", required = false) Long beforeSequence,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        MessageHistoryResponse response = messageQueryService.getHistory(currentUser.getUserId(), conversationId, beforeSequence, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/messages/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId
    ) {
        MessageResponse response = messageQueryService.getMessage(currentUser.getUserId(), messageId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/v1/messages/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @Valid @RequestBody EditMessageRequest request
    ) {
        MessageResponse response = messageEditService.editMessage(currentUser.getUserId(), messageId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/v1/messages/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @RequestParam(name = "scope", defaultValue = "FOR_ME") String scopeStr
    ) {
        DeleteScope scope;
        try {
            scope = DeleteScope.valueOf(scopeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            scope = DeleteScope.FOR_ME;
        }

        messageDeletionService.deleteMessage(currentUser.getUserId(), messageId, scope);
        return ResponseEntity.ok(Map.of("message", "Message deleted with scope " + scope.name()));
    }
}
