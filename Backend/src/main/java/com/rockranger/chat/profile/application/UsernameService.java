package com.rockranger.chat.profile.application;

import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.profile.dto.UsernameCheckResponse;
import com.rockranger.chat.rate_limit.RateLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
public class UsernameService {

    private final UserProfileRepository profileRepository;
    private final RateLimitService rateLimitService;
    private final AccountStatusGuard accountStatusGuard;
    private final AppProperties appProperties;

    public UsernameService(UserProfileRepository profileRepository,
                           RateLimitService rateLimitService,
                           AccountStatusGuard accountStatusGuard,
                           AppProperties appProperties) {
        this.profileRepository = profileRepository;
        this.rateLimitService = rateLimitService;
        this.accountStatusGuard = accountStatusGuard;
        this.appProperties = appProperties;
    }

    public UsernameCheckResponse checkAvailability(String username) {
        if (username == null || username.isBlank()) {
            return new UsernameCheckResponse(username, false);
        }

        String normalized = NormalizationUtil.normalizeUsername(username);
        if (!NormalizationUtil.isValidUsername(normalized)) {
            return new UsernameCheckResponse(normalized, false);
        }

        boolean taken = profileRepository.existsByUsernameNormalized(normalized);
        return new UsernameCheckResponse(normalized, !taken);
    }

    @Transactional
    public void changeUsername(CurrentUser currentUser, String newUsername) {
        accountStatusGuard.checkActive(currentUser);

        String normalized = NormalizationUtil.normalizeUsername(newUsername);
        if (!NormalizationUtil.isValidUsername(normalized)) {
            throw new ApiException(ErrorCode.INVALID_USERNAME);
        }

        UserProfile profile = profileRepository.findById(currentUser.getUserId())
                .orElse(new UserProfile(currentUser.getUserId()));

        if (normalized.equals(profile.getUsernameNormalized())) {
            return; // Unchanged idempotent
        }

        // Check 30-day cooldown policy (§ 10.2)
        if (profile.getUsernameChangedAt() != null) {
            int cooldownDays = appProperties.getUsernamePolicy().getCooldownDays();
            Instant nextAllowedAt = profile.getUsernameChangedAt().plus(cooldownDays, ChronoUnit.DAYS);
            if (Instant.now().isBefore(nextAllowedAt)) {
                throw new ApiException(
                        ErrorCode.USERNAME_CHANGE_COOLDOWN,
                        "Your username cannot be changed yet.",
                        Map.of("code", "USERNAME_CHANGE_COOLDOWN", "nextAllowedAt", nextAllowedAt.toString())
                );
            }
        }

        if (profileRepository.existsByUsernameNormalized(normalized)) {
            throw new ApiException(ErrorCode.USERNAME_ALREADY_TAKEN);
        }

        profile.setUsername(newUsername.trim());
        profile.setUsernameNormalized(normalized);
        profile.setUsernameChangedAt(Instant.now());
        profileRepository.save(profile);
    }
}
