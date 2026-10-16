package com.rockranger.chat.identity.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.identity.application.GoogleLoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/me/identities/google")
public class GoogleIdentityController {

    private final GoogleLoginService googleLoginService;

    public GoogleIdentityController(GoogleLoginService googleLoginService) {
        this.googleLoginService = googleLoginService;
    }

    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> linkGoogleIdentity(@AuthenticationPrincipal CurrentUser currentUser,
                                                                 @RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        googleLoginService.linkGoogleIdentity(currentUser, idToken);
        return ResponseEntity.ok(Map.of("message", "Google account successfully linked."));
    }

    @DeleteMapping
    public ResponseEntity<Void> unlinkGoogleIdentity(@AuthenticationPrincipal CurrentUser currentUser) {
        googleLoginService.unlinkGoogleIdentity(currentUser);
        return ResponseEntity.noContent().build();
    }
}
