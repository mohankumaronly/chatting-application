package com.rockranger.chat.conversation.application;

import com.rockranger.chat.block.application.BlockPolicy;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.domain.*;
import com.rockranger.chat.conversation.dto.ConversationMemberResponse;
import com.rockranger.chat.conversation.dto.ConversationResponse;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import com.rockranger.chat.user.domain.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationUserStateRepository userStateRepository;
    private final ConversationAuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final BlockPolicy blockPolicy;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationMemberRepository memberRepository,
            ConversationUserStateRepository userStateRepository,
            ConversationAuthorizationService authorizationService,
            UserRepository userRepository,
            BlockPolicy blockPolicy
    ) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.userStateRepository = userStateRepository;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.blockPolicy = blockPolicy;
    }

    @Transactional
    public ConversationResponse getOrCreateDirectConversation(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot create a conversation with yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Target user not found"));

        if (targetUser.getStatus() == UserStatus.DEACTIVATED) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Target user account has been deleted");
        }

        if (blockPolicy.hasAnyBlock(currentUserId, targetUserId)) {
            throw new ApiException(ErrorCode.USER_BLOCKED, "Cannot create conversation with a blocked user");
        }

        String pairKey = Conversation.calculateDirectPairKey(currentUserId, targetUserId);
        Optional<Conversation> existingOpt = conversationRepository.findByDirectPairKey(pairKey);

        if (existingOpt.isPresent()) {
            Conversation conv = existingOpt.get();

            // Ensure caller member record is active
            ConversationMember callerMember = memberRepository
                    .findByConversationIdAndUserId(conv.getId(), currentUserId)
                    .orElseGet(() -> new ConversationMember(conv.getId(), currentUserId, ConversationMemberRole.MEMBER));
            callerMember.setLeftAt(null);
            memberRepository.save(callerMember);

            // Ensure target member record is active
            ConversationMember targetMember = memberRepository
                    .findByConversationIdAndUserId(conv.getId(), targetUserId)
                    .orElseGet(() -> new ConversationMember(conv.getId(), targetUserId, ConversationMemberRole.MEMBER));
            targetMember.setLeftAt(null);
            memberRepository.save(targetMember);

            // Unhide in user state if previously hidden
            userStateRepository.findByConversationIdAndUserId(conv.getId(), currentUserId).ifPresent(state -> {
                if (state.getHiddenAt() != null) {
                    state.setHiddenAt(null);
                    state.setUpdatedAt(Instant.now());
                    userStateRepository.save(state);
                }
            });

            List<ConversationMemberResponse> members = memberRepository.findByConversationId(conv.getId()).stream()
                    .map(m -> new ConversationMemberResponse(m.getUserId(), m.getRole().name(), m.getJoinedAt()))
                    .toList();

            return new ConversationResponse(
                    conv.getId(),
                    conv.getType().name(),
                    false,
                    members,
                    conv.getCreatedAt(),
                    conv.getLastMessageAt()
            );
        }

        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, ConversationType.DIRECT, currentUserId, pairKey);
        conversationRepository.save(conversation);

        ConversationMember callerMember = new ConversationMember(conversationId, currentUserId, ConversationMemberRole.MEMBER);
        ConversationMember targetMember = new ConversationMember(conversationId, targetUserId, ConversationMemberRole.MEMBER);
        memberRepository.saveAll(List.of(callerMember, targetMember));

        ConversationUserState callerState = new ConversationUserState(conversationId, currentUserId);
        ConversationUserState targetState = new ConversationUserState(conversationId, targetUserId);
        userStateRepository.saveAll(List.of(callerState, targetState));

        List<ConversationMemberResponse> members = List.of(
                new ConversationMemberResponse(currentUserId, ConversationMemberRole.MEMBER.name(), callerMember.getJoinedAt()),
                new ConversationMemberResponse(targetUserId, ConversationMemberRole.MEMBER.name(), targetMember.getJoinedAt())
        );

        return new ConversationResponse(
                conversationId,
                ConversationType.DIRECT.name(),
                true,
                members,
                conversation.getCreatedAt(),
                conversation.getLastMessageAt()
        );
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID currentUserId, UUID conversationId) {
        authorizationService.requireCanView(currentUserId, conversationId);

        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found"));

        List<ConversationMemberResponse> members = memberRepository.findByConversationId(conv.getId()).stream()
                .filter(ConversationMember::isActive)
                .map(m -> new ConversationMemberResponse(m.getUserId(), m.getRole().name(), m.getJoinedAt()))
                .toList();

        return new ConversationResponse(
                conv.getId(),
                conv.getType().name(),
                false,
                members,
                conv.getCreatedAt(),
                conv.getLastMessageAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listUserConversations(UUID currentUserId) {
        List<ConversationMember> activeMemberships = memberRepository.findActiveByUserId(currentUserId);
        return activeMemberships.stream()
                .map(membership -> conversationRepository.findById(membership.getConversationId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(conv -> {
                    List<ConversationMemberResponse> members = memberRepository.findByConversationId(conv.getId()).stream()
                            .filter(ConversationMember::isActive)
                            .map(m -> new ConversationMemberResponse(m.getUserId(), m.getRole().name(), m.getJoinedAt()))
                            .toList();
                    return new ConversationResponse(
                            conv.getId(),
                            conv.getType().name(),
                            false,
                            members,
                            conv.getCreatedAt(),
                            conv.getLastMessageAt()
                    );
                })
                .toList();
    }
}
