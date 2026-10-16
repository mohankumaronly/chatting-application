package com.rockranger.chat.auth.application;

import com.rockranger.chat.auth.dto.AuthResponse;
import com.rockranger.chat.auth.dto.LoginRequest;
import com.rockranger.chat.auth.dto.UserSummaryDto;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.JwtTokenService;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.identity.domain.IdentityProvider;
import com.rockranger.chat.identity.domain.UserIdentity;
import com.rockranger.chat.identity.domain.UserIdentityRepository;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.session.application.SessionService;
import com.rockranger.chat.session.domain.ClientType;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserIdentityRepository identityRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;
    private final AccountStatusGuard accountStatusGuard;
    private final RateLimitService rateLimitService;
    private final AppProperties appProperties;

    public LoginService(UserRepository userRepository,
                        UserProfileRepository profileRepository,
                        UserIdentityRepository identityRepository,
                        PasswordEncoder passwordEncoder,
                        SessionService sessionService,
                        JwtTokenService jwtTokenService,
                        AccountStatusGuard accountStatusGuard,
                        RateLimitService rateLimitService,
                        AppProperties appProperties) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
        this.accountStatusGuard = accountStatusGuard;
        this.rateLimitService = rateLimitService;
        this.appProperties = appProperties;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String clientIp = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";
        rateLimitService.enforce("chat:rl:login:ip:" + clientIp, 30, Duration.ofMinutes(15));

        String rawIdentifier = request.getIdentifier().trim();
        Optional<User> userOpt;

        if (rawIdentifier.contains("@")) {
            String email = NormalizationUtil.normalizeEmail(rawIdentifier);
            userOpt = userRepository.findByEmailNormalized(email);
        } else {
            String usernameNorm = NormalizationUtil.normalizeUsername(rawIdentifier);
            userOpt = profileRepository.findByUsernameNormalized(usernameNorm)
                    .flatMap(profile -> userRepository.findById(profile.getUserId()));
        }

        if (userOpt.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userOpt.get();

        Optional<UserIdentity> identityOpt = identityRepository.findByUserIdAndProvider(user.getId(), IdentityProvider.LOCAL);
        if (identityOpt.isEmpty() || identityOpt.get().getPasswordHash() == null) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), identityOpt.get().getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Enforce account status (PENDING_DELETION check after credential match per § 12.7)
        accountStatusGuard.checkCanAuthenticate(user);

        ClientType clientType = ClientType.fromString(request.getClientType());
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "Unknown Device";
        if (userAgent == null || userAgent.length() > 150) {
            userAgent = "Client";
        }

        SessionService.SessionCreationResult sessionResult = sessionService.createSession(
                user.getId(),
                userAgent,
                clientType,
                request.isKeepLoggedIn()
        );

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), sessionResult.session().getId());

        if (httpResponse != null && clientType == ClientType.WEB) {
            setRefreshCookie(httpResponse, sessionResult.rawRefreshToken());
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

        String returnRefreshToken = (clientType != ClientType.WEB) ? sessionResult.rawRefreshToken() : null;
        return new AuthResponse(userSummary, accessToken, returnRefreshToken, appProperties.getJwt().getAccessTokenTtlSeconds());
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
