package com.rockranger.chat.auth.application;

import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.identity.domain.IdentityProvider;
import com.rockranger.chat.identity.domain.UserIdentity;
import com.rockranger.chat.identity.domain.UserIdentityRepository;
import com.rockranger.chat.notification.application.EmailOutboxService;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.verification.application.OtpService;
import com.rockranger.chat.verification.domain.ChallengePurpose;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class EmailChangeService {

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final OtpService otpService;
    private final EmailOutboxService emailOutboxService;
    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    public EmailChangeService(UserRepository userRepository,
                              UserIdentityRepository identityRepository,
                              OtpService otpService,
                              EmailOutboxService emailOutboxService,
                              StringRedisTemplate redisTemplate,
                              AppProperties appProperties) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
        this.otpService = otpService;
        this.emailOutboxService = emailOutboxService;
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
    }

    @Transactional
    public void startEmailChange(CurrentUser currentUser, String newEmail) {
        String normalized = NormalizationUtil.normalizeEmail(newEmail);
        if (!NormalizationUtil.isValidEmail(normalized)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Invalid email format");
        }

        if (userRepository.existsByEmailNormalized(normalized)) {
            throw new ApiException(ErrorCode.CONFLICT, "Email is already taken by another account");
        }

        OtpService.OtpGenerationResult otpResult = otpService.createUserOtp(currentUser.getUserId(), ChallengePurpose.EMAIL_CHANGE);

        redisTemplate.opsForValue().set("chat:email-change:" + currentUser.getUserId(), normalized, Duration.ofMinutes(15));

        String subject = "Verify Your New Email - " + appProperties.getName();
        String htmlContent = "<p>Your email change verification code is: <strong>" + otpResult.rawOtp() + "</strong></p>";
        emailOutboxService.enqueueEmail(normalized, "email-change-otp", subject, htmlContent);
    }

    @Transactional
    public void verifyEmailChange(CurrentUser currentUser, String newEmail, String otp) {
        String normalized = NormalizationUtil.normalizeEmail(newEmail);
        String pendingEmail = redisTemplate.opsForValue().get("chat:email-change:" + currentUser.getUserId());

        if (pendingEmail == null || !pendingEmail.equalsIgnoreCase(normalized)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "No email change request matching this address");
        }

        otpService.verifyUserOtp(currentUser.getUserId(), ChallengePurpose.EMAIL_CHANGE, otp);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        String oldEmail = user.getEmailNormalized();
        user.setEmailNormalized(normalized);
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        // Update local identity subject if local login is used
        Optional<UserIdentity> localIdentity = identityRepository.findByUserIdAndProvider(user.getId(), IdentityProvider.LOCAL);
        localIdentity.ifPresent(identity -> {
            identity.setProviderSubject(normalized);
            identityRepository.save(identity);
        });

        redisTemplate.delete("chat:email-change:" + currentUser.getUserId());

        // Notify both old and new emails
        String subject = "Email Address Updated";
        String content = "<p>Your email address for " + appProperties.getName() + " was changed to " + normalized + ".</p>";
        emailOutboxService.enqueueEmail(oldEmail, "email-changed-notice", subject, content);
        emailOutboxService.enqueueEmail(normalized, "email-changed-notice", subject, content);
    }
}
