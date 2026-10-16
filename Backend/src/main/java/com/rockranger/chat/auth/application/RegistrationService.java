package com.rockranger.chat.auth.application;

import com.rockranger.chat.account.domain.DeletedUserIdentityRepository;
import com.rockranger.chat.auth.dto.*;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
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
import com.rockranger.chat.verification.application.OtpService;
import com.rockranger.chat.verification.domain.RegistrationChallenge;
import com.rockranger.chat.verification.domain.RegistrationChallengeRepository;
import com.rockranger.chat.verification.domain.RegistrationChallengeStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final RegistrationChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserIdentityRepository identityRepository;
    private final DeletedUserIdentityRepository tombstoneRepository;
    private final OtpService otpService;
    private final EmailOutboxService emailOutboxService;
    private final RateLimitService rateLimitService;
    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public RegistrationService(RegistrationChallengeRepository challengeRepository,
                               UserRepository userRepository,
                               UserProfileRepository profileRepository,
                               UserIdentityRepository identityRepository,
                               DeletedUserIdentityRepository tombstoneRepository,
                               OtpService otpService,
                               EmailOutboxService emailOutboxService,
                               RateLimitService rateLimitService,
                               SessionService sessionService,
                               JwtTokenService jwtTokenService,
                               PasswordEncoder passwordEncoder,
                               AppProperties appProperties) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.identityRepository = identityRepository;
        this.tombstoneRepository = tombstoneRepository;
        this.otpService = otpService;
        this.emailOutboxService = emailOutboxService;
        this.rateLimitService = rateLimitService;
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Transactional
    public RegistrationStartResponse startRegistration(RegistrationStartRequest request,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        String email = NormalizationUtil.normalizeEmail(request.getEmail());
        if (!NormalizationUtil.isValidEmail(email)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Invalid email address format");
        }

        // Validate date of birth (must be at least 13 years old)
        if (request.getDateOfBirth() != null) {
            Period age = Period.between(request.getDateOfBirth(), LocalDate.now());
            if (age.getYears() < 13) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "You must be at least 13 years old to register.");
            }
        }

        // Rate limiting by IP and by Email
        String clientIp = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";
        String ipKey = "chat:rl:registration:ip:" + HmacUtil.computeSha256Hex(clientIp);
        rateLimitService.enforce(ipKey, 20, Duration.ofMinutes(15));

        String emailKey = "chat:rl:otp:send:" + HmacUtil.computeSha256Hex(email);
        rateLimitService.enforce(emailKey, 5, Duration.ofMinutes(15));

        // Check Tombstones: If permanently deleted previously, return generic success without sending email (Section 12.5)
        String tombstoneHash = HmacUtil.computeHmacHex(appProperties.getKeys().getIdentifierHmacBase64(), email);
        if (tombstoneRepository.existsByEmailHash(tombstoneHash)) {
            UUID mockId = UUID.randomUUID();
            String mockToken = SecureRandomUtil.generateRandomToken(32);
            return new RegistrationStartResponse(mockId, mockToken, "OTP_SENT", appProperties.getRegistration().getChallengeTtlSeconds(), "VERIFY_EMAIL");
        }

        // Check if already active
        if (userRepository.existsByEmailNormalized(email)) {
            // Return generic response without disclosing
            UUID mockId = UUID.randomUUID();
            String mockToken = SecureRandomUtil.generateRandomToken(32);
            return new RegistrationStartResponse(mockId, mockToken, "OTP_SENT", appProperties.getRegistration().getChallengeTtlSeconds(), "VERIFY_EMAIL");
        }

        UUID registrationId = UUID.randomUUID();
        String challengeToken = SecureRandomUtil.generateRandomToken(32);
        String challengeTokenHash = HmacUtil.computeSha256Hex(challengeToken);

        long ttlSeconds = appProperties.getRegistration().getChallengeTtlSeconds();
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);

        RegistrationChallenge challenge = new RegistrationChallenge(
                registrationId, email, request.getDateOfBirth(), challengeTokenHash, expiresAt
        );
        challengeRepository.save(challenge);

        // Generate and record OTP
        OtpService.OtpGenerationResult otpResult = otpService.createRegistrationOtp(registrationId);

        // Send OTP email
        String subject = "Your Verification Code - " + appProperties.getName();
        String htmlContent = "<p>Your verification code is: <strong>" + otpResult.rawOtp() + "</strong></p>" +
                "<p>This code will expire in " + (appProperties.getOtp().getTtlSeconds() / 60) + " minutes.</p>";
        emailOutboxService.enqueueEmail(email, "registration-otp", subject, htmlContent);

        if (httpResponse != null) {
            setChallengeCookie(httpResponse, challengeToken);
        }

        return new RegistrationStartResponse(registrationId, challengeToken, "OTP_SENT", ttlSeconds, "VERIFY_EMAIL");
    }

    @Transactional
    public void resendOtp(UUID registrationId, String challengeToken) {
        RegistrationChallenge challenge = getValidatedChallenge(registrationId, challengeToken);

        if (challenge.getStatus() != RegistrationChallengeStatus.OTP_SENT) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot resend OTP for this challenge stage");
        }

        String cooldownKey = "chat:cooldown:otp:" + registrationId;
        long cooldownSec = appProperties.getOtp().getResendCooldownSeconds();
        boolean allowed = rateLimitService.setCooldownIfNotPresent(cooldownKey, Duration.ofSeconds(cooldownSec));
        if (!allowed) {
            throw new ApiException(ErrorCode.OTP_COOLDOWN, "Please wait before requesting another verification code.");
        }

        OtpService.OtpGenerationResult otpResult = otpService.createRegistrationOtp(registrationId);

        String subject = "Your New Verification Code - " + appProperties.getName();
        String htmlContent = "<p>Your new verification code is: <strong>" + otpResult.rawOtp() + "</strong></p>";
        emailOutboxService.enqueueEmail(challenge.getEmailLookup(), "registration-otp", subject, htmlContent);
    }

    @Transactional
    public OtpVerifyResponse verifyOtp(UUID registrationId, String challengeToken, String otp) {
        RegistrationChallenge challenge = getValidatedChallenge(registrationId, challengeToken);

        otpService.verifyRegistrationOtp(registrationId, otp);

        challenge.setStatus(RegistrationChallengeStatus.EMAIL_VERIFIED);
        challenge.setVerifiedAt(Instant.now());
        challengeRepository.save(challenge);

        return new OtpVerifyResponse("EMAIL_VERIFIED", "COMPLETE_REGISTRATION");
    }

    @Transactional
    public RegistrationCompleteResponse completeRegistration(UUID registrationId,
                                                             String challengeToken,
                                                             RegistrationCompleteRequest request) {
        RegistrationChallenge challenge = getValidatedChallenge(registrationId, challengeToken);

        if (challenge.getStatus() != RegistrationChallengeStatus.EMAIL_VERIFIED) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Email must be verified before completing registration.");
        }

        String email = challenge.getEmailLookup();
        if (userRepository.existsByEmailNormalized(email)) {
            throw new ApiException(ErrorCode.CONFLICT, "An account with this email already exists.");
        }

        UUID userId = UUID.randomUUID();
        User user = new User(
                userId,
                email,
                challenge.getDateOfBirth(),
                request.getFirstName().trim(),
                request.getLastName().trim(),
                UserStatus.PENDING_SETUP
        );
        user.setEmailVerifiedAt(challenge.getVerifiedAt());
        userRepository.save(user);

        // Store Local identity with hashed password
        String passwordHash = passwordEncoder.encode(request.getPassword());
        UserIdentity identity = new UserIdentity(
                UUID.randomUUID(),
                userId,
                IdentityProvider.LOCAL,
                email,
                passwordHash
        );
        identityRepository.save(identity);

        // Create empty profile
        UserProfile profile = new UserProfile(userId);
        profileRepository.save(profile);

        challenge.setStatus(RegistrationChallengeStatus.COMPLETED);
        challenge.setCompletedAt(Instant.now());
        challengeRepository.save(challenge);

        return new RegistrationCompleteResponse(registrationId, "PENDING_SETUP", "SET_USERNAME");
    }

    public RegistrationStatusResponse getRegistrationStatus(UUID registrationId, String challengeToken) {
        RegistrationChallenge challenge = getValidatedChallenge(registrationId, challengeToken);
        long remaining = Math.max(0, Duration.between(Instant.now(), challenge.getExpiresAt()).getSeconds());

        String nextStep;
        switch (challenge.getStatus()) {
            case OTP_SENT -> nextStep = "VERIFY_EMAIL";
            case EMAIL_VERIFIED -> nextStep = "COMPLETE_REGISTRATION";
            case COMPLETED -> nextStep = "SET_USERNAME";
            default -> nextStep = "EXPIRED";
        }

        return new RegistrationStatusResponse(registrationId, challenge.getEmailLookup(), challenge.getStatus().name(), nextStep, remaining);
    }

    @Transactional
    public void setInitialUsername(UUID registrationId, String challengeToken, String username) {
        RegistrationChallenge challenge = getValidatedChallenge(registrationId, challengeToken);

        String normalized = NormalizationUtil.normalizeUsername(username);
        if (!NormalizationUtil.isValidUsername(normalized)) {
            throw new ApiException(ErrorCode.INVALID_USERNAME);
        }

        if (profileRepository.existsByUsernameNormalized(normalized)) {
            throw new ApiException(ErrorCode.USERNAME_ALREADY_TAKEN);
        }

        User user = userRepository.findByEmailNormalized(challenge.getEmailLookup())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User record not found"));

        UserProfile profile = profileRepository.findById(user.getId())
                .orElse(new UserProfile(user.getId()));

        profile.setUsername(username.trim());
        profile.setUsernameNormalized(normalized);
        profile.setUsernameChangedAt(Instant.now());
        profileRepository.save(profile);
    }

    @Transactional
    public AuthResponse finishRegistration(UUID registrationId,
                                          String challengeToken,
                                          RegistrationFinishRequest request,
                                          HttpServletResponse httpResponse) {
        RegistrationChallenge challenge = getValidatedChallenge(registrationId, challengeToken);

        User user = userRepository.findByEmailNormalized(challenge.getEmailLookup())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        UserProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Profile not found"));

        if (profile.getUsernameNormalized() == null) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_ACTIVE, "A valid username is mandatory to activate account.");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Create initial session
        SessionService.SessionCreationResult sessionResult = sessionService.createSession(
                user.getId(), "Web Browser", ClientType.WEB, true
        );

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), sessionResult.session().getId());

        if (httpResponse != null) {
            setRefreshCookie(httpResponse, sessionResult.rawRefreshToken());
        }

        // Enqueue welcome email
        String subject = "Welcome to " + appProperties.getName();
        String htmlContent = "<p>Welcome " + user.getFirstName() + "! Your account setup is complete.</p>";
        emailOutboxService.enqueueEmail(user.getEmailNormalized(), "welcome", subject, htmlContent);

        UserSummaryDto summary = new UserSummaryDto(
                user.getId(),
                profile.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmailNormalized(),
                user.getStatus().name(),
                true
        );

        return new AuthResponse(summary, accessToken, sessionResult.rawRefreshToken(), appProperties.getJwt().getAccessTokenTtlSeconds());
    }

    private RegistrationChallenge getValidatedChallenge(UUID registrationId, String challengeToken) {
        RegistrationChallenge challenge = challengeRepository.findById(registrationId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHALLENGE_INVALID, "Invalid registration challenge ID"));

        if (challenge.isExpired()) {
            throw new ApiException(ErrorCode.CHALLENGE_EXPIRED);
        }

        if (challengeToken != null && !challengeToken.isBlank()) {
            String tokenHash = HmacUtil.computeSha256Hex(challengeToken);
            if (!HmacUtil.constantTimeEquals(tokenHash, challenge.getChallengeTokenHash())) {
                throw new ApiException(ErrorCode.CHALLENGE_INVALID, "Challenge verification token mismatch");
            }
        }

        return challenge;
    }

    private void setChallengeCookie(HttpServletResponse response, String challengeToken) {
        Cookie cookie = new Cookie("registration_token", challengeToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(appProperties.getCookie().isSecure());
        cookie.setPath(appProperties.getCookie().getPath());
        cookie.setMaxAge((int) appProperties.getRegistration().getChallengeTtlSeconds());
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
