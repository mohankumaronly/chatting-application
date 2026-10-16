package com.rockranger.chat.conversation.application;

import com.rockranger.chat.block.application.BlockPolicy;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.conversation.domain.ConversationMemberRepository;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ConversationAuthorizationService {

    private final ConversationMemberRepository memberRepository;
    private final BlockPolicy blockPolicy;
    private final AccountStatusGuard accountStatusGuard;
    private final UserRepository userRepository;

    public ConversationAuthorizationService(
            ConversationMemberRepository memberRepository,
            BlockPolicy blockPolicy,
            AccountStatusGuard accountStatusGuard,
            UserRepository userRepository
    ) {
        this.memberRepository = memberRepository;
        this.blockPolicy = blockPolicy;
        this.accountStatusGuard = accountStatusGuard;
        this.userRepository = userRepository;
    }

    public void requireCanSend(UUID userId, UUID conversationId) {
        requireMembership(userId, conversationId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        accountStatusGuard.checkActive(user);

        Set<UUID> otherMembers = memberRepository.findOtherActiveMemberIds(conversationId, userId);
        for (UUID otherId : otherMembers) {
            if (blockPolicy.hasAnyBlock(userId, otherId)) {
                throw new ApiException(ErrorCode.USER_BLOCKED, "Cannot send messages to a blocked user");
            }
        }
    }

    public void requireCanView(UUID userId, UUID conversationId) {
        requireMembership(userId, conversationId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        accountStatusGuard.checkActive(user);
    }

    public void requireMembership(UUID userId, UUID conversationId) {
        boolean isMember = memberRepository.existsActive(conversationId, userId);
        if (!isMember) {
            throw new ApiException(ErrorCode.CONVERSATION_ACCESS_DENIED, "You are not an active member of this conversation");
        }
    }
}
