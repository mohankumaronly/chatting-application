package com.rockranger.chat.account.application;

import com.rockranger.chat.account.domain.*;
import com.rockranger.chat.account.dto.DeletionRequestRequest;
import com.rockranger.chat.account.dto.DeletionRequestResponse;
import com.rockranger.chat.account.dto.DeletionStatusResponse;
import com.rockranger.chat.audit.SecurityAuditService;
import com.rockranger.chat.auth.dto.AuthResponse;
import com.rockranger.chat.auth.dto.UserSummaryDto;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.security.JwtTokenService;
import com.rockranger.chat.common.util.HmacUtil;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.common.util.SecureRandomUtil;
import com.rockranger.chat.identity.domain.IdentityProvider;
import com.rockranger.chat.identity.domain.UserIdentity;
import com.rockranger.chat.identity.domain.UserIdentityRepository;
import com.rockranger.chat.notification.application.EmailOutboxService;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.session.application.SessionService;
import com.rockranger.chat.session.domain.ClientType;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.user.domain.UserStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountDeletionService {

    private final AccountDeletionRequestRepository deletionRepository;
    private final DeletedUserIdentityRepository tombstoneRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserIdentityRepository identityRepository;
    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;
    private final EmailOutboxService emailOutboxService;
    private final RateLimitService rateLimitService;
    private final SecurityAuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AccountDeletionService(AccountDeletionRequestRepository deletionRepository,
                                  DeletedUserIdentityRepository tombstoneRepository,
                                  UserRepository userRepository,
                                  UserProfileRepository profileRepository,
                                  UserIdentityRepository identityRepository,
                                  SessionService sessionService,
                                  JwtTokenService jwtTokenService,
                                  EmailOutboxService emailOutboxService,
                                  RateLimitService rateLimitService,
                                  SecurityAuditService auditService,
                                  PasswordEncoder passwordEncoder,
                                  AppProperties appProperties) {
        this.deletionRepository = deletionRepository;
        this.tombstoneRepository = tombstoneRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.identityRepository = identityRepository;
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
        this.emailOutboxService = emailOutboxService;
        this.rateLimitService = rateLimitService;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Transactional
    public DeletionRequestResponse requestDeletion(CurrentUser currentUser,
                                                  DeletionRequestRequest request,
                                                  HttpServletResponse response) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_ACTIVE, "Only active accounts can request deletion");
        }

        // Verify password if local credentials exist
        Optional<UserIdentity> localIdentity = identityRepository.findByUserIdAndProvider(user.getId(), IdentityProvider.LOCAL);
        if (localIdentity.isPresent() && localIdentity.get().getPasswordHash() != null) {
            if (request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), localIdentity.get().getPasswordHash())) {
                throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Incorrect password entered for account deletion");
            }
        }

        // Check if active deletion request already exists
        if (deletionRepository.findByUserIdAndStatus(user.getId(), AccountDeletionStatus.PENDING).isPresent()) {
            throw new ApiException(ErrorCode.DELETION_REQUEST_EXISTS);
        }

        int graceDays = appProperties.getAccountDeletion().getGraceDays();
        Instant recoverableUntil = Instant.now().plus(graceDays, ChronoUnit.DAYS);

        // Generate recovery token (32 random bytes, base64url)
        String rawRecoveryToken = SecureRandomUtil.generateRandomToken(32);
        String tokenHash = HmacUtil.computeHmacHex(appProperties.getRecoveryToken().getHmacKeyBase64(), rawRecoveryToken);

        AccountDeletionRequest deletionRequest = new AccountDeletionRequest(
                UUID.randomUUID(),
                user.getId(),
                recoverableUntil,
                request.getReason(),
                tokenHash,
                recoverableUntil
        );
        deletionRepository.save(deletionRequest);

        // Transition user status to PENDING_DELETION
        user.setStatus(UserStatus.PENDING_DELETION);
        userRepository.save(user);

        // Revoke all active sessions
        sessionService.revokeAllUserSessions(user.getId());

        if (response != null) {
            clearRefreshCookie(response);
        }

        // Enqueue deletion requested notification with recovery token
        String subject = "Account Deletion Request Received - " + appProperties.getName();
        String htmlContent = "<p>Your account deletion request has been registered.</p>" +
                "<p>Your account will be permanently deleted after " + graceDays + " days.</p>" +
                "<p>To cancel deletion and recover your account, submit this recovery token: <strong>" +
                rawRecoveryToken + "</strong></p>";
        emailOutboxService.enqueueEmail(user.getEmailNormalized(), "deletion-requested", subject, htmlContent);

        auditService.logEvent("ACCOUNT_DELETION_REQUESTED", user.getId(), "Account deletion requested. Recoverable until: " + recoverableUntil);

        return new DeletionRequestResponse(
                "PENDING_DELETION",
                recoverableUntil,
                "Your account will be permanently deleted on " + recoverableUntil + ". Sign in or recover before then."
        );
    }

    public DeletionRequestResponse getDeletionInfo(CurrentUser currentUser) {
        AccountDeletionRequest request = deletionRepository.findByUserIdAndStatus(currentUser.getUserId(), AccountDeletionStatus.PENDING)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "No active deletion request found for account"));

        return new DeletionRequestResponse(
                request.getStatus().name(),
                request.getRecoverableUntil(),
                "Account is scheduled for deletion on " + request.getRecoverableUntil()
        );
    }

    @Transactional
    public AuthResponse recoverAccount(String rawRecoveryToken, HttpServletResponse response) {
        String tokenHash = HmacUtil.computeHmacHex(appProperties.getRecoveryToken().getHmacKeyBase64(), rawRecoveryToken);

        Optional<AccountDeletionRequest> requestOpt = deletionRepository.findByRecoveryTokenHashAndStatus(tokenHash, AccountDeletionStatus.PENDING);
        if (requestOpt.isEmpty()) {
            auditService.logEvent("ACCOUNT_DELETION_RECOVERY_FAILED", null, "Invalid or unknown recovery token");
            throw new ApiException(ErrorCode.DELETION_RECOVERY_INVALID);
        }

        AccountDeletionRequest deletionRequest = requestOpt.get();
        if (Instant.now().isAfter(deletionRequest.getRecoverableUntil())) {
            throw new ApiException(ErrorCode.DELETION_RECOVERY_EXPIRED);
        }

        User user = userRepository.findById(deletionRequest.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User record not found"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        deletionRequest.setStatus(AccountDeletionStatus.RECOVERED);
        deletionRequest.setCompletedAt(Instant.now());
        deletionRequest.setRecoveryTokenHash(null);
        deletionRepository.save(deletionRequest);

        // Create fresh session
        SessionService.SessionCreationResult sessionResult = sessionService.createSession(
                user.getId(), "Recovered Web Client", ClientType.WEB, true
        );

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), sessionResult.session().getId());

        if (response != null) {
            setRefreshCookie(response, sessionResult.rawRefreshToken());
        }

        // Enqueue account recovered email
        String subject = "Account Recovered - " + appProperties.getName();
        String htmlContent = "<p>Your account deletion has been cancelled. Your account is now active again.</p>";
        emailOutboxService.enqueueEmail(user.getEmailNormalized(), "account-recovered", subject, htmlContent);

        auditService.logEvent("ACCOUNT_DELETION_RECOVERED", user.getId(), "Account recovered successfully");

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

        return new AuthResponse(summary, accessToken, sessionResult.rawRefreshToken(), appProperties.getJwt().getAccessTokenTtlSeconds());
    }

    public DeletionStatusResponse checkDeletionStatus(String email, HttpServletRequest request) {
        String clientIp = request != null ? request.getRemoteAddr() : "127.0.0.1";
        rateLimitService.enforce("chat:rl:deletion:status:" + clientIp, 20, Duration.ofHours(1));

        String normalized = NormalizationUtil.normalizeEmail(email);
        Optional<User> userOpt = userRepository.findByEmailNormalized(normalized);

        if (userOpt.isEmpty()) {
            return new DeletionStatusResponse(false);
        }

        User user = userOpt.get();
        boolean isPendingDeletion = user.getStatus() == UserStatus.PENDING_DELETION;
        return new DeletionStatusResponse(isPendingDeletion);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(appProperties.getCookie().isSecure());
        cookie.setPath(appProperties.getCookie().getPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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
