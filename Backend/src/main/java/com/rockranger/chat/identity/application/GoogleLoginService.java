package com.rockranger.chat.identity.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.chat.audit.SecurityAuditService;
import com.rockranger.chat.auth.dto.AuthResponse;
import com.rockranger.chat.auth.dto.UserSummaryDto;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.security.JwtTokenService;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.identity.domain.IdentityProvider;
import com.rockranger.chat.identity.domain.UserIdentity;
import com.rockranger.chat.identity.domain.UserIdentityRepository;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.session.application.SessionService;
import com.rockranger.chat.session.domain.ClientType;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.user.domain.UserStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleLoginService {

    private final UserIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;
    private final AccountStatusGuard accountStatusGuard;
    private final SecurityAuditService auditService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    public GoogleLoginService(UserIdentityRepository identityRepository,
                              UserRepository userRepository,
                              UserProfileRepository profileRepository,
                              SessionService sessionService,
                              JwtTokenService jwtTokenService,
                              AccountStatusGuard accountStatusGuard,
                              SecurityAuditService auditService,
                              AppProperties appProperties,
                              ObjectMapper objectMapper) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
        this.accountStatusGuard = accountStatusGuard;
        this.auditService = auditService;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public record GoogleTokenPayload(String sub, String email, String name, String givenName, String familyName) {}

    @Transactional
    public AuthResponse loginWithGoogleIdToken(String idToken,
                                              String clientTypeStr,
                                              boolean keepLoggedIn,
                                              HttpServletResponse response) {
        GoogleTokenPayload payload = verifyGoogleIdToken(idToken);
        Optional<UserIdentity> identityOpt = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, payload.sub());

        User user;
        if (identityOpt.isPresent()) {
            user = userRepository.findById(identityOpt.get().getUserId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User record not found"));
            accountStatusGuard.checkCanAuthenticate(user);
        } else {
            // New user registration or check if local account exists
            String normalizedEmail = NormalizationUtil.normalizeEmail(payload.email());
            if (userRepository.existsByEmailNormalized(normalizedEmail)) {
                // Section 9.2: Do not automatically link Google identity to existing local account!
                throw new ApiException(ErrorCode.CONFLICT, "An account with this email already exists. Please sign in with your password and link Google in Settings.");
            }

            UUID newUserId = UUID.randomUUID();
            String firstName = payload.givenName() != null ? payload.givenName() : (payload.name() != null ? payload.name() : "User");
            String lastName = payload.familyName() != null ? payload.familyName() : "";

            user = new User(newUserId, normalizedEmail, null, firstName, lastName, UserStatus.PENDING_SETUP);
            user.setEmailVerifiedAt(Instant.now());
            userRepository.save(user);

            UserIdentity identity = new UserIdentity(UUID.randomUUID(), newUserId, IdentityProvider.GOOGLE, payload.sub(), null);
            identityRepository.save(identity);

            UserProfile profile = new UserProfile(newUserId);
            profileRepository.save(profile);
        }

        ClientType clientType = ClientType.fromString(clientTypeStr);
        SessionService.SessionCreationResult sessionResult = sessionService.createSession(
                user.getId(), "Google Auth Client", clientType, keepLoggedIn
        );

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), sessionResult.session().getId());

        if (clientType == ClientType.WEB && response != null) {
            setRefreshCookie(response, sessionResult.rawRefreshToken());
        }

        Optional<UserProfile> profileOpt = profileRepository.findById(user.getId());
        String username = profileOpt.map(UserProfile::getUsername).orElse(null);

        UserSummaryDto summary = new UserSummaryDto(
                user.getId(),
                username,
                user.getFirstName(),
                user.getLastName(),
                user.getEmailNormalized(),
                user.getStatus().name(),
                username != null
        );

        String returnRefreshToken = (clientType != ClientType.WEB) ? sessionResult.rawRefreshToken() : null;
        return new AuthResponse(summary, accessToken, returnRefreshToken, appProperties.getJwt().getAccessTokenTtlSeconds());
    }

    @Transactional
    public void linkGoogleIdentity(CurrentUser currentUser, String idToken) {
        GoogleTokenPayload payload = verifyGoogleIdToken(idToken);

        Optional<UserIdentity> existing = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, payload.sub());
        if (existing.isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "This Google account is already linked to another user.");
        }

        UserIdentity identity = new UserIdentity(
                UUID.randomUUID(),
                currentUser.getUserId(),
                IdentityProvider.GOOGLE,
                payload.sub(),
                null
        );
        identityRepository.save(identity);
        auditService.logEvent("GOOGLE_IDENTITY_LINKED", currentUser.getUserId(), "Google identity linked with sub: " + payload.sub());
    }

    @Transactional
    public void unlinkGoogleIdentity(CurrentUser currentUser) {
        long count = identityRepository.countByUserId(currentUser.getUserId());
        if (count <= 1) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot remove your only sign-in method.");
        }

        identityRepository.deleteByUserIdAndProvider(currentUser.getUserId(), IdentityProvider.GOOGLE);
        auditService.logEvent("GOOGLE_IDENTITY_UNLINKED", currentUser.getUserId(), "Google identity unlinked.");
    }

    private GoogleTokenPayload verifyGoogleIdToken(String idToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid Google ID token");
            }

            JsonNode node = objectMapper.readTree(resp.body());
            String sub = node.path("sub").asText(null);
            String email = node.path("email").asText(null);
            String emailVerified = node.path("email_verified").asText("false");
            String aud = node.path("aud").asText(null);

            if (sub == null || email == null || !"true".equalsIgnoreCase(emailVerified)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Unverified or invalid Google token claims");
            }

            if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.startsWith("replace_")) {
                if (!googleClientId.equals(aud)) {
                    throw new ApiException(ErrorCode.UNAUTHORIZED, "Google token audience mismatch");
                }
            }

            String name = node.path("name").asText(null);
            String givenName = node.path("given_name").asText(null);
            String familyName = node.path("family_name").asText(null);

            return new GoogleTokenPayload(sub, email, name, givenName, familyName);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Failed to verify Google token: " + e.getMessage());
        }
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
