package com.rockranger.chat.presence.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.presence.application.PresenceService;
import com.rockranger.chat.presence.dto.PresenceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/{userId}/presence")
    public ResponseEntity<PresenceResponse> getPresence(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        PresenceResponse response = presenceService.getPresence(currentUser.getUserId(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/presence/heartbeat")
    public ResponseEntity<Map<String, String>> heartbeat(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        presenceService.heartbeat(currentUser.getUserId(), currentUser.getSessionId());
        return ResponseEntity.ok(Map.of("message", "Heartbeat received"));
    }
}
