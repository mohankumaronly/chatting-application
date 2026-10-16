package com.rockranger.chat.auth.application;

import com.rockranger.chat.auth.dto.AuthResponse;
import com.rockranger.chat.auth.dto.UserSummaryDto;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.JwtTokenService;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.session.application.SessionService;
import com.rockranger.chat.session.domain.ClientType;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TokenRefreshService {

    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AccountStatusGuard accountStatusGuard;
    private final AppProperties appProperties;

    public TokenRefreshService(SessionService sessionService,
                               JwtTokenService jwtTokenService,
                               UserRepository userRepository,
                               UserProfileRepository profileRepository,
                               AccountStatusGuard accountStatusGuard,
                               AppProperties appProperties) {
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.accountStatusGuard = accountStatusGuard;
        this.appProperties = appProperties;
    }

    @Transactional
    public AuthResponse refresh(String requestRefreshToken,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        String rawToken = requestRefreshToken;
        if (rawToken == null || rawToken.isBlank()) {
            // Check cookie
            if (request != null && request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refresh_token".equals(cookie.getName())) {
                        rawToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Refresh token is missing");
        }

        String userAgent = request != null ? request.getHeader("User-Agent") : "Client";
        Optional<SessionService.SessionCreationResult> rotateResult = sessionService.rotateSession(rawToken, userAgent);

        if (rotateResult.isEmpty()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Refresh session is expired or invalid");
        }

        SessionService.SessionCreationResult newSession = rotateResult.get();
        User user = userRepository.findById(newSession.session().getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "User not found"));

        // Check user status guard (Section 12.7)
        accountStatusGuard.checkCanAuthenticate(user);

        String newAccessToken = jwtTokenService.generateAccessToken(user.getId(), newSession.session().getId());

        boolean isWeb = newSession.session().getClientType() == ClientType.WEB;
        if (isWeb && response != null) {
            setRefreshCookie(response, newSession.rawRefreshToken());
        }

        Optional<UserProfile> profileOpt = profileRepository.findById(user.getId());
        String username = profileOpt.map(UserProfile::getUsername).orElse(null);

        UserSummaryDto userSummary = new UserSummaryDto(
                user.getId(),
                username,
                user.getFirstName(),
                user.getLastName(),
                user.getEmailNormalized(),
                user.getStatus().name(),
                username != null
        );

        String returnRefreshToken = (!isWeb) ? newSession.rawRefreshToken() : null;
        return new AuthResponse(userSummary, newAccessToken, returnRefreshToken, appProperties.getJwt().getAccessTokenTtlSeconds());
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(appProperties.getCookie().isSecure());
        cookie.setPath(appProperties.getCookie().getPath());
        cookie.setMaxAge((int) appProperties.getRefreshToken().getTtlSeconds());
        response.addCookie(cookie);
    }
}
