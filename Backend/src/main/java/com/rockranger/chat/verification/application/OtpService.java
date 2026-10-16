package com.rockranger.chat.verification.application;

import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.util.HmacUtil;
import com.rockranger.chat.common.util.SecureRandomUtil;
import com.rockranger.chat.verification.domain.ChallengePurpose;
import com.rockranger.chat.verification.domain.OtpChallenge;
import com.rockranger.chat.verification.domain.OtpChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {

    private final OtpChallengeRepository otpRepository;
    private final AppProperties appProperties;

    public OtpService(OtpChallengeRepository otpRepository, AppProperties appProperties) {
        this.otpRepository = otpRepository;
        this.appProperties = appProperties;
    }

    public record OtpGenerationResult(UUID challengeId, String rawOtp) {}

    @Transactional
    public OtpGenerationResult createRegistrationOtp(UUID registrationId) {
        UUID challengeId = UUID.randomUUID();
        String rawOtp = SecureRandomUtil.generateNumericOtp(6);
        String otpHash = computeOtpHash(challengeId, ChallengePurpose.REGISTRATION, rawOtp);

        long ttl = appProperties.getOtp().getTtlSeconds();
        Instant expiresAt = Instant.now().plusSeconds(ttl);

        OtpChallenge challenge = new OtpChallenge(challengeId, registrationId, null, ChallengePurpose.REGISTRATION, otpHash, expiresAt);
        otpRepository.save(challenge);

        return new OtpGenerationResult(challengeId, rawOtp);
    }

    @Transactional
    public OtpGenerationResult createUserOtp(UUID userId, ChallengePurpose purpose) {
        UUID challengeId = UUID.randomUUID();
        String rawOtp = SecureRandomUtil.generateNumericOtp(6);
        String otpHash = computeOtpHash(challengeId, purpose, rawOtp);

        long ttl = appProperties.getOtp().getTtlSeconds();
        Instant expiresAt = Instant.now().plusSeconds(ttl);

        OtpChallenge challenge = new OtpChallenge(challengeId, null, userId, purpose, otpHash, expiresAt);
        otpRepository.save(challenge);

        return new OtpGenerationResult(challengeId, rawOtp);
    }

    @Transactional
    public void verifyRegistrationOtp(UUID registrationId, String rawOtp) {
        Optional<OtpChallenge> challengeOpt = otpRepository
                .findTopByRegistrationIdAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(registrationId, ChallengePurpose.REGISTRATION);

        if (challengeOpt.isEmpty()) {
            throw new ApiException(ErrorCode.CHALLENGE_EXPIRED, "No active OTP verification challenge found");
        }

        verifyAndConsume(challengeOpt.get(), ChallengePurpose.REGISTRATION, rawOtp);
    }

    @Transactional
    public void verifyUserOtp(UUID userId, ChallengePurpose purpose, String rawOtp) {
        Optional<OtpChallenge> challengeOpt = otpRepository
                .findTopByUserIdAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(userId, purpose);

        if (challengeOpt.isEmpty()) {
            throw new ApiException(ErrorCode.CHALLENGE_EXPIRED, "No active OTP verification challenge found");
        }

        verifyAndConsume(challengeOpt.get(), purpose, rawOtp);
    }

    private void verifyAndConsume(OtpChallenge challenge, ChallengePurpose purpose, String rawOtp) {
        if (challenge.isExpired()) {
            throw new ApiException(ErrorCode.CHALLENGE_EXPIRED, "The OTP code has expired. Please request a new one.");
        }

        int maxAttempts = appProperties.getOtp().getMaxAttempts();
        if (challenge.getAttemptCount() >= maxAttempts) {
            throw new ApiException(ErrorCode.OTP_ATTEMPTS_EXCEEDED, "Maximum OTP verification attempts reached.");
        }

        challenge.setAttemptCount(challenge.getAttemptCount() + 1);

        String expectedHash = computeOtpHash(challenge.getId(), purpose, rawOtp);
        if (!HmacUtil.constantTimeEquals(expectedHash, challenge.getOtpHash())) {
            otpRepository.save(challenge);
            throw new ApiException(ErrorCode.OTP_INVALID, "Invalid verification code.");
        }

        challenge.setConsumedAt(Instant.now());
        otpRepository.save(challenge);
    }

    private String computeOtpHash(UUID challengeId, ChallengePurpose purpose, String rawOtp) {
        String key = appProperties.getKeys().getOtpHmacBase64();
        String data = challengeId.toString() + ":" + purpose.name() + ":" + rawOtp;
        return HmacUtil.computeHmacHex(key, data);
    }
}
