package com.rockranger.chat.reaction.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.reaction.application.MessageReactionService;
import com.rockranger.chat.reaction.dto.AddReactionRequest;
import com.rockranger.chat.reaction.dto.MessageReactionsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages/{messageId}/reactions")
public class MessageReactionController {

    private final MessageReactionService reactionService;

    public MessageReactionController(MessageReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping
    public ResponseEntity<MessageReactionsResponse> addReaction(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @Valid @RequestBody AddReactionRequest request
    ) {
        MessageReactionsResponse response = reactionService.addReaction(
                currentUser.getUserId(),
                messageId,
                request
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{emoji}")
    public ResponseEntity<MessageReactionsResponse> removeReaction(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @PathVariable String emoji
    ) {
        MessageReactionsResponse response = reactionService.removeReaction(
                currentUser.getUserId(),
                messageId,
                emoji
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<MessageReactionsResponse> getReactions(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId
    ) {
        MessageReactionsResponse response = reactionService.getReactions(
                currentUser.getUserId(),
                messageId
        );
        return ResponseEntity.ok(response);
    }
}
