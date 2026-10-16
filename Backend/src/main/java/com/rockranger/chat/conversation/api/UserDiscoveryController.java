package com.rockranger.chat.conversation.api;

import com.rockranger.chat.block.application.BlockPolicy;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.conversation.dto.PublicUserProfileResponse;
import com.rockranger.chat.conversation.dto.UserSearchResponse;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.user.domain.UserStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserDiscoveryController {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final BlockPolicy blockPolicy;
    private final RateLimitService rateLimitService;

    public UserDiscoveryController(
            UserProfileRepository userProfileRepository,
            UserRepository userRepository,
            BlockPolicy blockPolicy,
            RateLimitService rateLimitService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.blockPolicy = blockPolicy;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUsers(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        rateLimitService.enforce("chat:rl:search:" + currentUser.getUserId(), 60, Duration.ofMinutes(1));

        String trimmed = query.trim();
        if (trimmed.length() < 2) {
            return ResponseEntity.ok(new UserSearchResponse(List.of(), null));
        }

        int boundedLimit = Math.min(Math.max(limit, 1), 50);
        List<UserProfile> profiles = userProfileRepository.searchByUsername(trimmed, PageRequest.of(0, boundedLimit));

        List<UserSearchResponse.Item> items = new ArrayList<>();
        for (UserProfile profile : profiles) {
            if (profile.getUserId().equals(currentUser.getUserId())) {
                continue; // Do not include oneself in search
            }
            if (blockPolicy.hasAnyBlock(currentUser.getUserId(), profile.getUserId())) {
                continue; // Do not show blocked users
            }

            User user = userRepository.findById(profile.getUserId()).orElse(null);
            if (user == null || user.getStatus() == UserStatus.DEACTIVATED) {
                continue;
            }

            String displayName = (user.getFirstName() != null)
                    ? (user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "")).trim()
                    : profile.getUsername();
            items.add(new UserSearchResponse.Item(
                    profile.getUserId(),
                    profile.getUsername(),
                    displayName,
                    profile.getAvatarUrl(),
                    profile.getBio()
            ));
        }

        return ResponseEntity.ok(new UserSearchResponse(items, null));
    }

    @GetMapping("/{userId}/public-profile")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        if (blockPolicy.hasAnyBlock(currentUser.getUserId(), userId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "User profile not found");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new ApiException(ErrorCode.NOT_FOUND, "User not found");
        }

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        String username = profile != null ? profile.getUsername() : user.getEmailNormalized();
        String bio = profile != null ? profile.getBio() : null;
        String avatarUrl = profile != null ? profile.getAvatarUrl() : null;

        String displayName = (user.getFirstName() != null)
                ? (user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "")).trim()
                : username;

        return ResponseEntity.ok(new PublicUserProfileResponse(
                userId,
                username,
                displayName,
                avatarUrl,
                bio
        ));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<PublicUserProfileResponse> getProfileByUsername(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable String username
    ) {
        String normalized = username.toLowerCase().trim();
        UserProfile profile = userProfileRepository.findByUsernameNormalized(normalized)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User profile not found"));

        return getPublicProfile(currentUser, profile.getUserId());
    }
}
