package com.rockranger.chat.profile.application;

import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.profile.dto.ProfileResponse;
import com.rockranger.chat.profile.dto.UpdateProfileRequest;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AccountStatusGuard accountStatusGuard;
    private final AppProperties appProperties;

    public ProfileService(UserProfileRepository profileRepository,
                          UserRepository userRepository,
                          AccountStatusGuard accountStatusGuard,
                          AppProperties appProperties) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.accountStatusGuard = accountStatusGuard;
        this.appProperties = appProperties;
    }

    public ProfileResponse getProfile(CurrentUser currentUser) {
        accountStatusGuard.checkActive(currentUser);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        UserProfile profile = profileRepository.findById(currentUser.getUserId())
                .orElse(new UserProfile(currentUser.getUserId()));

        return toProfileResponse(user, profile);
    }

    @Transactional
    public ProfileResponse updateProfile(CurrentUser currentUser, UpdateProfileRequest request) {
        accountStatusGuard.checkActive(currentUser);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        UserProfile profile = profileRepository.findById(currentUser.getUserId())
                .orElse(new UserProfile(currentUser.getUserId()));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }

        userRepository.save(user);
        profileRepository.save(profile);

        return toProfileResponse(user, profile);
    }

    @Transactional
    public ProfileResponse updateAvatar(CurrentUser currentUser, String avatarPublicId, String avatarUrl) {
        accountStatusGuard.checkActive(currentUser);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        UserProfile profile = profileRepository.findById(currentUser.getUserId())
                .orElse(new UserProfile(currentUser.getUserId()));

        validateCloudinaryOwnership(avatarPublicId, avatarUrl);

        profile.setAvatarPublicId(avatarPublicId);
        profile.setAvatarUrl(avatarUrl);
        profile.setUpdatedAt(java.time.Instant.now());
        profileRepository.save(profile);

        return toProfileResponse(user, profile);
    }

    @Transactional
    public ProfileResponse removeAvatar(CurrentUser currentUser) {
        accountStatusGuard.checkActive(currentUser);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        UserProfile profile = profileRepository.findById(currentUser.getUserId())
                .orElse(new UserProfile(currentUser.getUserId()));

        profile.setAvatarPublicId(null);
        profile.setAvatarUrl(null);
        profile.setUpdatedAt(java.time.Instant.now());
        profileRepository.save(profile);

        return toProfileResponse(user, profile);
    }

    private void validateCloudinaryOwnership(String avatarPublicId, String avatarUrl) {
        if (avatarPublicId == null || avatarUrl == null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Avatar public ID and URL are required");
        }

        String cloudName = appProperties.getCloudinary().getCloudName();
        String folder = appProperties.getCloudinary().getAvatarFolder();

        String expectedUrlPrefix = "https://res.cloudinary.com/" + cloudName + "/image/upload/";
        if (!avatarUrl.startsWith(expectedUrlPrefix)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Avatar URL must belong to this application's Cloudinary cloud");
        }

        String expectedIdPrefix = folder.endsWith("/") ? folder : folder + "/";
        if (!avatarPublicId.startsWith(expectedIdPrefix)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Avatar public ID must be inside the " + folder + " folder");
        }
    }

    private ProfileResponse toProfileResponse(User user, UserProfile profile) {
        boolean profileComplete = profile.getUsernameNormalized() != null;
        return new ProfileResponse(
                user.getId(),
                profile.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmailNormalized(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profileComplete,
                profile.getUsernameChangedAt()
        );
    }
}