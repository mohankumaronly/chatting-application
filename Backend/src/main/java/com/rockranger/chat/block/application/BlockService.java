package com.rockranger.chat.block.application;

import com.rockranger.chat.audit.SecurityAuditService;
import com.rockranger.chat.block.domain.UserBlock;
import com.rockranger.chat.block.domain.UserBlockRepository;
import com.rockranger.chat.block.dto.BlockResponse;
import com.rockranger.chat.block.dto.BlockStatusResponse;
import com.rockranger.chat.block.dto.PagedBlockResponse;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlockService implements BlockPolicy {

    private final UserBlockRepository blockRepository;
    private final UserRepository userRepository;
    private final AccountStatusGuard accountStatusGuard;
    private final RateLimitService rateLimitService;
    private final SecurityAuditService auditService;
    private final AppProperties appProperties;

    public BlockService(UserBlockRepository blockRepository,
                        UserRepository userRepository,
                        AccountStatusGuard accountStatusGuard,
                        RateLimitService rateLimitService,
                        SecurityAuditService auditService,
                        AppProperties appProperties) {
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.accountStatusGuard = accountStatusGuard;
        this.rateLimitService = rateLimitService;
        this.auditService = auditService;
        this.appProperties = appProperties;
    }

    public record BlockExecutionResult(BlockResponse response, boolean created) {}

    @Transactional
    public BlockExecutionResult blockUser(CurrentUser currentUser, UUID blockedUserId, String reason) {
        accountStatusGuard.checkActive(currentUser);

        if (currentUser.getUserId().equals(blockedUserId)) {
            throw new ApiException(ErrorCode.SELF_BLOCK_FORBIDDEN);
        }

        // Target user must exist and be active or pending setup (cannot block DEACTIVATED or PENDING_DELETION per § 11.2)
        Optional<User> targetOpt = userRepository.findById(blockedUserId);
        if (targetOpt.isEmpty() ||
                targetOpt.get().getStatus() == UserStatus.DEACTIVATED ||
                targetOpt.get().getStatus() == UserStatus.PENDING_DELETION) {
            throw new ApiException(ErrorCode.NOT_FOUND, "User not found");
        }

        // Rate limit: 100 per hour
        int maxPerHour = appProperties.getBlock().getMaxPerHour();
        rateLimitService.enforce("chat:rl:block:" + currentUser.getUserId(), maxPerHour, Duration.ofHours(1));

        // Idempotent: if already blocked, return existing block (Section 11.2 & 15.8)
        Optional<UserBlock> existingOpt = blockRepository.findByBlockerUserIdAndBlockedUserId(currentUser.getUserId(), blockedUserId);
        if (existingOpt.isPresent()) {
            UserBlock existing = existingOpt.get();
            return new BlockExecutionResult(new BlockResponse(existing.getId(), existing.getBlockedUserId(), existing.getCreatedAt()), false);
        }

        UserBlock newBlock = new UserBlock(UUID.randomUUID(), currentUser.getUserId(), blockedUserId, reason);
        blockRepository.save(newBlock);

        auditService.logEvent("BLOCK_CREATED", currentUser.getUserId(), "Blocked user: " + blockedUserId);

        return new BlockExecutionResult(new BlockResponse(newBlock.getId(), newBlock.getBlockedUserId(), newBlock.getCreatedAt()), true);
    }

    @Transactional
    public void unblockUser(CurrentUser currentUser, UUID blockedUserId) {
        accountStatusGuard.checkActive(currentUser);

        Optional<UserBlock> blockOpt = blockRepository.findByBlockerUserIdAndBlockedUserId(currentUser.getUserId(), blockedUserId);
        if (blockOpt.isEmpty()) {
            throw new ApiException(ErrorCode.BLOCK_NOT_FOUND);
        }

        blockRepository.delete(blockOpt.get());
        auditService.logEvent("BLOCK_REMOVED", currentUser.getUserId(), "Unblocked user: " + blockedUserId);
    }

    public PagedBlockResponse listBlocks(CurrentUser currentUser, Pageable pageable) {
        accountStatusGuard.checkActive(currentUser);

        Page<UserBlock> page = blockRepository.findAllByBlockerUserId(currentUser.getUserId(), pageable);
        List<BlockResponse> dtos = page.getContent().stream()
                .map(b -> new BlockResponse(b.getId(), b.getBlockedUserId(), b.getCreatedAt()))
                .collect(Collectors.toList());

        return new PagedBlockResponse(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public BlockStatusResponse getBlockStatus(CurrentUser currentUser, UUID targetUserId) {
        accountStatusGuard.checkActive(currentUser);

        boolean blocked = blockRepository.existsByBlockerUserIdAndBlockedUserId(currentUser.getUserId(), targetUserId);
        boolean blockedBy = blockRepository.existsByBlockerUserIdAndBlockedUserId(targetUserId, currentUser.getUserId());

        return new BlockStatusResponse(blocked, blockedBy);
    }

    // --- BlockPolicy Implementation for Chat Module Integration (§ 11.3) ---

    @Override
    public boolean isBlocked(UUID userId, UUID otherUserId) {
        return blockRepository.existsByBlockerUserIdAndBlockedUserId(userId, otherUserId);
    }

    @Override
    public Set<UUID> filterBlocked(Set<UUID> candidateIds, UUID viewerId) {
        return candidateIds.stream()
                .filter(candidateId -> !isBlocked(viewerId, candidateId) && !isBlocked(candidateId, viewerId))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasAnyBlock(UUID userId, UUID otherUserId) {
        return isBlocked(userId, otherUserId) || isBlocked(otherUserId, userId);
    }
}
