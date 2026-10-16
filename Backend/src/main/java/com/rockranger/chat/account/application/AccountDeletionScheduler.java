package com.rockranger.chat.account.application;

import com.rockranger.chat.account.domain.*;
import com.rockranger.chat.audit.SecurityAuditService;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.util.HmacUtil;
import com.rockranger.chat.notification.application.EmailOutboxService;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.user.domain.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountDeletionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletionScheduler.class);

    private final AccountDeletionRequestRepository deletionRepository;
    private final DeletedUserIdentityRepository tombstoneRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final EmailOutboxService emailOutboxService;
    private final SecurityAuditService auditService;
    private final AppProperties appProperties;

    public AccountDeletionScheduler(AccountDeletionRequestRepository deletionRepository,
                                    DeletedUserIdentityRepository tombstoneRepository,
                                    UserRepository userRepository,
                                    UserProfileRepository profileRepository,
                                    EmailOutboxService emailOutboxService,
                                    SecurityAuditService auditService,
                                    AppProperties appProperties) {
        this.deletionRepository = deletionRepository;
        this.tombstoneRepository = tombstoneRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.emailOutboxService = emailOutboxService;
        this.auditService = auditService;
        this.appProperties = appProperties;
    }

    @Scheduled(cron = "${app.account-deletion.scheduler-cron:0 0 * * * *}")
    @Transactional
    public void processPermanentDeletions() {
        int batchSize = appProperties.getAccountDeletion().getBatchSize();
        List<AccountDeletionRequest> expiredRequests = deletionRepository.findExpiredPendingRequests(
                Instant.now(), PageRequest.of(0, batchSize)
        );

        if (expiredRequests.isEmpty()) {
            return;
        }

        log.info("Processing {} expired account deletion requests for permanent anonymization and tombstoning.", expiredRequests.size());

        for (AccountDeletionRequest request : expiredRequests) {
            try {
                Optional<User> userOpt = userRepository.findById(request.getUserId());
                if (userOpt.isEmpty()) {
                    request.setStatus(AccountDeletionStatus.COMPLETED);
                    request.setCompletedAt(Instant.now());
                    deletionRepository.save(request);
                    continue;
                }

                User user = userOpt.get();

                // Idempotency guard (§ 12.4): Skip if not in PENDING_DELETION
                if (user.getStatus() != UserStatus.PENDING_DELETION) {
                    request.setStatus(AccountDeletionStatus.CANCELLED);
                    deletionRepository.save(request);
                    continue;
                }

                String originalEmail = user.getEmailNormalized();
                Optional<UserProfile> profileOpt = profileRepository.findById(user.getId());
                String usernameNormalized = profileOpt.map(UserProfile::getUsernameNormalized).orElse(null);

                // Insert tombstone row with HMAC'd email (§ 12.5)
                String emailHmac = HmacUtil.computeHmacHex(appProperties.getKeys().getIdentifierHmacBase64(), originalEmail);
                DeletedUserIdentity tombstone = new DeletedUserIdentity(
                        UUID.randomUUID(),
                        user.getId(),
                        emailHmac,
                        usernameNormalized
                );
                tombstoneRepository.save(tombstone);

                // Anonymize user row
                user.setEmailNormalized("deleted-" + user.getId() + "@deleted.local");
                user.setFirstName("Deleted");
                user.setLastName("User");
                user.setDateOfBirth(null);
                user.setStatus(UserStatus.DEACTIVATED);
                user.setUpdatedAt(Instant.now());
                userRepository.save(user);

                // Clear profile bio & avatar
                profileOpt.ifPresent(profile -> {
                    profile.setBio(null);
                    profile.setAvatarPublicId(null);
                    profileRepository.save(profile);
                });

                // Mark deletion request as COMPLETED
                request.setStatus(AccountDeletionStatus.COMPLETED);
                request.setCompletedAt(Instant.now());
                request.setRecoveryTokenHash(null);
                deletionRepository.save(request);

                // Send final completion notice to original email
                String subject = "Your Account Has Been Permanently Deleted";
                String content = "<p>Your account deletion grace period has ended and all personal data has been permanently anonymized.</p>";
                emailOutboxService.enqueueEmail(originalEmail, "deletion-completed", subject, content);

                auditService.logEvent("ACCOUNT_PERMANENTLY_DELETED", user.getId(), "Account permanently anonymized and tombstoned.");
            } catch (Exception e) {
                log.error("Failed to process deletion request {}: {}", request.getId(), e.getMessage(), e);
            }
        }
    }
}
