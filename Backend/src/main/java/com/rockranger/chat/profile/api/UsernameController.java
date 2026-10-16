package com.rockranger.chat.profile.api;

import com.rockranger.chat.auth.dto.UsernameRequest;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.profile.application.UsernameService;
import com.rockranger.chat.profile.dto.UsernameCheckResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UsernameController {

    private final UsernameService usernameService;

    public UsernameController(UsernameService usernameService) {
        this.usernameService = usernameService;
    }

    @GetMapping("/api/v1/username/available")
    public ResponseEntity<UsernameCheckResponse> checkAvailability(@RequestParam("username") String username) {
        UsernameCheckResponse response = usernameService.checkAvailability(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/me/username")
    public ResponseEntity<Map<String, String>> setUsernamePost(@AuthenticationPrincipal CurrentUser currentUser,
                                                               @Valid @RequestBody UsernameRequest request) {
        usernameService.changeUsername(currentUser, request.getUsername());
        return ResponseEntity.ok(Map.of("message", "Username successfully updated.", "username", request.getUsername()));
    }

    @PatchMapping("/api/v1/me/username")
    public ResponseEntity<Map<String, String>> setUsernamePatch(@AuthenticationPrincipal CurrentUser currentUser,
                                                                @Valid @RequestBody UsernameRequest request) {
        usernameService.changeUsername(currentUser, request.getUsername());
        return ResponseEntity.ok(Map.of("message", "Username successfully updated.", "username", request.getUsername()));
    }
}
