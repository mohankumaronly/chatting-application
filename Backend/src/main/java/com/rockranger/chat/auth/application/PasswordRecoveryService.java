package com.rockranger.chat.auth.application;

import com.rockranger.chat.auth.dto.ResetVerifyResponse;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.util.HmacUtil;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.common.util.SecureRandomUtil;
import com.rockranger.chat.identity.domain.IdentityProvider;
import com.rockranger.chat.identity.domain.UserIdentity;
import com.rockranger.chat.identity.domain.UserIdentityRepository;
import com.rockranger.chat.notification.application.EmailOutboxService;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.session.application.SessionService;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.verification.application.OtpService;
import com.rockranger.chat.verification.domain.ChallengePurpose;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final OtpService otpService;
    private final EmailOutboxService emailOutboxService;
    private final SessionService sessionService;
    private final RateLimitService rateLimitService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public PasswordRecoveryService(UserRepository userRepository,
                                   UserIdentityRepository identityRepository,
                                   OtpService otpService,
                                   EmailOutboxService emailOutboxService,
                                   SessionService sessionService,
                                   RateLimitService rateLimitService,
                                   StringRedisTemplate redisTemplate,
                                   PasswordEncoder passwordEncoder,
                                   AppProperties appProperties) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
        this.otpService = otpService;
        this.emailOutboxService = emailOutboxService;
        this.sessionService = sessionService;
        this.rateLimitService = rateLimitService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Transactional
    public void forgotPassword(String email, HttpServletRequest httpRequest) {
        String normalizedEmail = NormalizationUtil.normalizeEmail(email);
        String clientIp = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";

        rateLimitService.enforce("chat:rl:password-reset:ip:" + clientIp, 10, Duration.ofMinutes(15));
        rateLimitService.enforce("chat:rl:password-reset:email:" + HmacUtil.computeSha256Hex(normalizedEmail), 5, Duration.ofMinutes(15));

        Optional<User> userOpt = userRepository.findByEmailNormalized(normalizedEmail);
        if (userOpt.isEmpty()) {
            // Generic response to prevent account enumeration (§ 7.3 & 16)
            return;
        }

        User user = userOpt.get();
        OtpService.OtpGenerationResult otpResult = otpService.createUserOtp(user.getId(), ChallengePurpose.PASSWORD_RESET);

        String subject = "Reset Your Password - " + appProperties.getName();
        String htmlContent = "<p>Your password reset code is: <strong>" + otpResult.rawOtp() + "</strong></p>" +
                "<p>This code will expire in " + (appProperties.getPasswordReset().getTtlSeconds() / 60) + " minutes.</p>";
        emailOutboxService.enqueueEmail(user.getEmailNormalized(), "password-reset-otp", subject, htmlContent);
    }

    @Transactional
    public ResetVerifyResponse verifyResetOtp(String email, String otp) {
        String normalizedEmail = NormalizationUtil.normalizeEmail(email);
        User user = userRepository.findByEmailNormalized(normalizedEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.OTP_INVALID, "Invalid verification code or email"));

        otpService.verifyUserOtp(user.getId(), ChallengePurpose.PASSWORD_RESET, otp);

        String resetToken = SecureRandomUtil.generateRandomToken(32);
        String tokenHash = HmacUtil.computeSha256Hex(resetToken);

        long ttlSeconds = appProperties.getPasswordReset().getTtlSeconds();
        redisTemplate.opsForValue().set("chat:reset-token:" + tokenHash, user.getId().toString(), Duration.ofSeconds(ttlSeconds));

        return new ResetVerifyResponse(resetToken, ttlSeconds);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        String tokenHash = HmacUtil.computeSha256Hex(resetToken);
        String redisKey = "chat:reset-token:" + tokenHash;
        String userIdStr = redisTemplate.opsForValue().get(redisKey);

        if (userIdStr == null) {
            throw new ApiException(ErrorCode.CHALLENGE_EXPIRED, "Invalid or expired reset token");
        }

        UUID userId = UUID.fromString(userIdStr);
        UserIdentity identity = identityRepository.findByUserIdAndProvider(userId, IdentityProvider.LOCAL)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Local credentials not found"));

        identity.setPasswordHash(passwordEncoder.encode(newPassword));
        identityRepository.save(identity);

        // Invalidate all active sessions for security (§ 7.3)
        sessionService.revokeAllUserSessions(userId);

        // Delete reset token (single use)
        redisTemplate.delete(redisKey);

        userRepository.findById(userId).ifPresent(user -> {
            String subject = "Your Password Has Been Changed";
            String content = "<p>Your password for " + appProperties.getName() + " was recently updated.</p>";
            emailOutboxService.enqueueEmail(user.getEmailNormalized(), "password-changed", subject, content);
        });
    }

    @Transactional
    public void changePassword(CurrentUser currentUser, String currentPassword, String newPassword) {
        UserIdentity identity = identityRepository.findByUserIdAndProvider(currentUser.getUserId(), IdentityProvider.LOCAL)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "Local password not set for this account"));

        if (!passwordEncoder.matches(currentPassword, identity.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Current password does not match");
        }

        identity.setPasswordHash(passwordEncoder.encode(newPassword));
        identityRepository.save(identity);

        // Revoke all sessions
        sessionService.revokeAllUserSessions(currentUser.getUserId());
    }
}
