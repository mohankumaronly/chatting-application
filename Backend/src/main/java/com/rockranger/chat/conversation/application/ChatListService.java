package com.rockranger.chat.conversation.application;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.conversation.domain.*;
import com.rockranger.chat.conversation.dto.ChatListItemResponse;
import com.rockranger.chat.conversation.dto.ChatListResponse;
import com.rockranger.chat.conversation.dto.UpdateChatSettingsRequest;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageRepository;
import com.rockranger.chat.messaging.domain.MessageUserDeletionRepository;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ChatListService {

    private final ConversationMemberRepository memberRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationUserStateRepository userStateRepository;
    private final MessageRepository messageRepository;
    private final MessageUserDeletionRepository userDeletionRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ConversationAuthorizationService authorizationService;

    public ChatListService(
            ConversationMemberRepository memberRepository,
            ConversationRepository conversationRepository,
            ConversationUserStateRepository userStateRepository,
            MessageRepository messageRepository,
            MessageUserDeletionRepository userDeletionRepository,
            UserProfileRepository userProfileRepository,
            UserRepository userRepository,
            ConversationAuthorizationService authorizationService
    ) {
        this.memberRepository = memberRepository;
        this.conversationRepository = conversationRepository;
        this.userStateRepository = userStateRepository;
        this.messageRepository = messageRepository;
        this.userDeletionRepository = userDeletionRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public ChatListResponse getChatList(UUID currentUserId, Boolean archived, int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 50);

        List<ConversationMember> memberships = memberRepository.findActiveByUserId(currentUserId);
        List<ChatListItemResponse> items = new ArrayList<>();

        for (ConversationMember member : memberships) {
            UUID convId = member.getConversationId();
            Optional<Conversation> convOpt = conversationRepository.findById(convId);
            if (convOpt.isEmpty()) continue;
            Conversation conv = convOpt.get();

            ConversationUserState state = userStateRepository
                    .findByConversationIdAndUserId(convId, currentUserId)
                    .orElseGet(() -> new ConversationUserState(convId, currentUserId));

            // Hidden chats are excluded from the main list
            if (state.isHidden()) continue;

            // Filter by archive status if requested
            if (archived != null) {
                if (archived && !state.isArchived()) continue;
                if (!archived && state.isArchived()) continue;
            }

            // Other user details for DIRECT chat
            ChatListItemResponse.OtherUserSummary otherUserSummary = null;
            if (conv.getType() == ConversationType.DIRECT) {
                Set<UUID> others = memberRepository.findOtherActiveMemberIds(convId, currentUserId);
                if (!others.isEmpty()) {
                    UUID otherId = others.iterator().next();
                    UserProfile profile = userProfileRepository.findById(otherId).orElse(null);
                    User user = userRepository.findById(otherId).orElse(null);

                    String username = profile != null ? profile.getUsername() : (user != null ? user.getEmailNormalized() : "User");
                    String displayName = (user != null && user.getFirstName() != null)
                            ? (user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "")).trim()
                            : username;
                    String avatarUrl = profile != null ? profile.getAvatarUrl() : null;

                    otherUserSummary = new ChatListItemResponse.OtherUserSummary(otherId, username, displayName, avatarUrl);
                }
            }

            // Last message preview
            ChatListItemResponse.LastMessageSummary lastMessageSummary = null;
            if (conv.getLastMessageId() != null) {
                Optional<Message> msgOpt = messageRepository.findById(conv.getLastMessageId());
                if (msgOpt.isPresent()) {
                    Message m = msgOpt.get();
                    boolean isDeletedForMe = userDeletionRepository.isMessageDeletedForUser(m.getId(), currentUserId);

                    if (!isDeletedForMe) {
                        String preview;
                        if (m.isDeleted()) {
                            preview = "This message was deleted";
                        } else if (m.getContent() != null && !m.getContent().isBlank()) {
                            preview = m.getContent().length() > 100 ? m.getContent().substring(0, 97) + "..." : m.getContent();
                        } else {
                            preview = "[" + m.getMessageType().name() + "]";
                        }
                        lastMessageSummary = new ChatListItemResponse.LastMessageSummary(
                                m.getId(),
                                m.getMessageType().name(),
                                preview,
                                m.getSenderId(),
                                m.getCreatedAt()
                        );
                    }
                }
            }

            // Unread count calculation
            long unreadCount = messageRepository.countUnreadMessages(convId, currentUserId, member.getLastReadAt());

            items.add(new ChatListItemResponse(
                    conv.getId(),
                    conv.getType().name(),
                    otherUserSummary,
                    lastMessageSummary,
                    unreadCount,
                    state.isArchived(),
                    state.isPinned(),
                    state.getMutedUntil()
            ));
        }

        // Sort: pinned first, then lastMessageAt DESC, then conversation creation DESC
        items.sort(Comparator
                .comparing(ChatListItemResponse::pinned, Comparator.reverseOrder())
                .thenComparing((ChatListItemResponse item) -> {
                    if (item.lastMessage() != null && item.lastMessage().createdAt() != null) {
                        return item.lastMessage().createdAt();
                    }
                    return Instant.EPOCH;
                }, Comparator.reverseOrder())
        );

        List<ChatListItemResponse> paginatedItems = items.stream().limit(boundedLimit).toList();
        String nextCursor = items.size() > boundedLimit ? String.valueOf(boundedLimit) : null;

        return new ChatListResponse(paginatedItems, nextCursor);
    }

    @Transactional
    public void updateSettings(UUID currentUserId, UUID conversationId, UpdateChatSettingsRequest req) {
        authorizationService.requireMembership(currentUserId, conversationId);

        ConversationUserState state = userStateRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseGet(() -> new ConversationUserState(conversationId, currentUserId));

        if (req.archived() != null) {
            state.setArchivedAt(req.archived() ? Instant.now() : null);
        }
        if (req.pinned() != null) {
            state.setPinnedAt(req.pinned() ? Instant.now() : null);
        }
        if (req.mutedUntil() != null) {
            state.setMutedUntil(req.mutedUntil());
        }
        state.setUpdatedAt(Instant.now());
        userStateRepository.save(state);
    }

    @Transactional
    public void setArchive(UUID currentUserId, UUID conversationId, boolean archive) {
        authorizationService.requireMembership(currentUserId, conversationId);
        ConversationUserState state = userStateRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseGet(() -> new ConversationUserState(conversationId, currentUserId));
        state.setArchivedAt(archive ? Instant.now() : null);
        state.setUpdatedAt(Instant.now());
        userStateRepository.save(state);
    }

    @Transactional
    public void setHide(UUID currentUserId, UUID conversationId, boolean hide) {
        authorizationService.requireMembership(currentUserId, conversationId);
        ConversationUserState state = userStateRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseGet(() -> new ConversationUserState(conversationId, currentUserId));
        state.setHiddenAt(hide ? Instant.now() : null);
        state.setUpdatedAt(Instant.now());
        userStateRepository.save(state);
    }
}
