package com.rockranger.chat.conversation.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.conversation.application.ConversationService;
import com.rockranger.chat.conversation.dto.ConversationResponse;
import com.rockranger.chat.conversation.dto.CreateDirectConversationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationResponse> createOrGetDirectConversation(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateDirectConversationRequest request
    ) {
        ConversationResponse response = conversationService.getOrCreateDirectConversation(
                currentUser.getUserId(),
                request.targetUserId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> listConversations(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        List<ConversationResponse> responses = conversationService.listUserConversations(currentUser.getUserId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID conversationId
    ) {
        ConversationResponse response = conversationService.getConversation(
                currentUser.getUserId(),
                conversationId
        );
        return ResponseEntity.ok(response);
    }
}
