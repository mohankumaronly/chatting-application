package com.rockranger.chat.auth.api;

import com.rockranger.chat.auth.application.LoginService;
import com.rockranger.chat.auth.application.LogoutService;
import com.rockranger.chat.auth.application.TokenRefreshService;
import com.rockranger.chat.auth.dto.AuthResponse;
import com.rockranger.chat.auth.dto.LoginRequest;
import com.rockranger.chat.auth.dto.NativeGoogleLoginRequest;
import com.rockranger.chat.auth.dto.SessionResponse;
import com.rockranger.chat.auth.dto.TokenRefreshRequest;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.identity.application.GoogleLoginService;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.session.application.SessionService;
import com.rockranger.chat.session.domain.UserSession;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final LoginService loginService;
    private final TokenRefreshService refreshService;
    private final LogoutService logoutService;
    private final GoogleLoginService googleLoginService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    public AuthenticationController(LoginService loginService,
                                  TokenRefreshService refreshService,
                                  LogoutService logoutService,
                                  GoogleLoginService googleLoginService,
                                  SessionService sessionService,
                                  UserRepository userRepository,
                                  UserProfileRepository profileRepository) {
        this.loginService = loginService;
        this.refreshService = refreshService;
        this.logoutService = logoutService;
        this.googleLoginService = googleLoginService;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        AuthResponse response = loginService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody(required = false) TokenRefreshRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        String token = request != null ? request.getRefreshToken() : null;
        AuthResponse response = refreshService.refresh(token, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CurrentUser currentUser,
                                       HttpServletResponse httpResponse) {
        logoutService.logout(currentUser, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CurrentUser currentUser,
                                          HttpServletResponse httpResponse) {
        logoutService.logoutAll(currentUser, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal CurrentUser currentUser) {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow();
        Optional<UserProfile> profileOpt = profileRepository.findById(currentUser.getUserId());

        Map<String, Object> profileMap = Map.of(
                "bio", profileOpt.map(UserProfile::getBio).orElse(null) != null ? profileOpt.get().getBio() : "",
                "avatarUrl", profileOpt.map(UserProfile::getAvatarPublicId).orElse(null) != null ? profileOpt.get().getAvatarPublicId() : ""
        );

        Map<String, Object> response = Map.of(
                "id", user.getId(),
                "email", user.getEmailNormalized(),
                "emailVerified", user.getEmailVerifiedAt() != null,
                "username", profileOpt.map(UserProfile::getUsername).orElse(""),
                "status", user.getStatus().name(),
                "profile", profileMap
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(@AuthenticationPrincipal CurrentUser currentUser) {
        List<UserSession> sessions = sessionService.getActiveSessions(currentUser.getUserId());
        List<SessionResponse> dtos = sessions.stream()
                .map(s -> new SessionResponse(
                        s.getId(),
                        s.getDeviceName(),
                        s.getClientType().name(),
                        s.getCreatedAt(),
                        s.getLastUsedAt(),
                        s.getId().equals(currentUser.getSessionId())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> revokeSession(@PathVariable("sessionId") UUID sessionId,
                                              @AuthenticationPrincipal CurrentUser currentUser) {
        sessionService.revokeSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/google/native")
    public ResponseEntity<AuthResponse> googleNativeLogin(@Valid @RequestBody NativeGoogleLoginRequest request,
                                                          HttpServletResponse httpResponse) {
        AuthResponse response = googleLoginService.loginWithGoogleIdToken(
                request.getIdToken(),
                request.getClientType(),
                request.isKeepLoggedIn(),
                httpResponse
        );
        return ResponseEntity.ok(response);
    }
}
