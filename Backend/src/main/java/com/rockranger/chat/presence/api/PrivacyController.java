package com.rockranger.chat.presence.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.presence.application.PresenceService;
import com.rockranger.chat.presence.dto.UpdatePrivacySettingsRequest;
import com.rockranger.chat.presence.dto.UserPresenceSettingsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/privacy")
public class PrivacyController {

    private final PresenceService presenceService;

    public PrivacyController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping
    public ResponseEntity<UserPresenceSettingsResponse> getPrivacySettings(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        UserPresenceSettingsResponse response = presenceService.getPrivacySettings(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<UserPresenceSettingsResponse> updatePrivacySettings(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestBody UpdatePrivacySettingsRequest request
    ) {
        UserPresenceSettingsResponse response = presenceService.updatePrivacySettings(
                currentUser.getUserId(),
                request
        );
        return ResponseEntity.ok(response);
    }
}
