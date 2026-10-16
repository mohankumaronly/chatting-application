package com.rockranger.chat.profile.api;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.profile.application.ProfileService;
import com.rockranger.chat.profile.dto.ProfileResponse;
import com.rockranger.chat.profile.dto.UpdateAvatarRequest;
import com.rockranger.chat.profile.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/profile")
public class MyProfileController {

    private final ProfileService profileService;

    public MyProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal CurrentUser currentUser) {
        ProfileResponse response = profileService.getProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal CurrentUser currentUser,
                                                         @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/avatar")
    public ResponseEntity<ProfileResponse> updateAvatar(@AuthenticationPrincipal CurrentUser currentUser,
                                                        @Valid @RequestBody UpdateAvatarRequest request) {
        ProfileResponse response = profileService.updateAvatar(
                currentUser,
                request.getAvatarPublicId(),
                request.getAvatarUrl()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<ProfileResponse> removeAvatar(@AuthenticationPrincipal CurrentUser currentUser) {
        ProfileResponse response = profileService.removeAvatar(currentUser);
        return ResponseEntity.ok(response);
    }
}