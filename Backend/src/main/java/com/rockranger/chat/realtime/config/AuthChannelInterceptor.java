package com.rockranger.chat.realtime.config;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.security.JwtTokenService;
import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.session.domain.UserSession;
import com.rockranger.chat.session.domain.UserSessionRepository;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthChannelInterceptor.class);

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSessionRepository userSessionRepository;
    private final AccountStatusGuard accountStatusGuard;
    private final ConversationAuthorizationService conversationAuthorizationService;

    public AuthChannelInterceptor(
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSessionRepository userSessionRepository,
            AccountStatusGuard accountStatusGuard,
            ConversationAuthorizationService conversationAuthorizationService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSessionRepository = userSessionRepository;
        this.accountStatusGuard = accountStatusGuard;
        this.conversationAuthorizationService = conversationAuthorizationService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || authHeader.isBlank()) {
                authHeader = accessor.getFirstNativeHeader("token");
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7).trim();
            }

            if (authHeader != null && !authHeader.isBlank()) {
                try {
                    Claims claims = jwtTokenService.parseAndValidateToken(authHeader);
                    String sub = claims.getSubject();
                    String sidStr = claims.get("sid", String.class);

                    if (sub != null && sidStr != null) {
                        UUID userId = UUID.fromString(sub);
                        UUID sessionId = UUID.fromString(sidStr);

                        Optional<UserSession> sessionOpt = userSessionRepository.findById(sessionId);
                        if (sessionOpt.isPresent() && sessionOpt.get().isActive()) {
                            Optional<User> userOpt = userRepository.findById(userId);
                            if (userOpt.isPresent()) {
                                User user = userOpt.get();
                                accountStatusGuard.checkCanAuthenticate(user);
                                accountStatusGuard.checkActive(user);

                                Optional<UserProfile> profileOpt = userProfileRepository.findById(userId);
                                String username = profileOpt.map(UserProfile::getUsername).orElse(null);

                                CurrentUser currentUser = new CurrentUser(
                                        user.getId(),
                                        sessionId,
                                        user.getEmailNormalized(),
                                        username,
                                        user.getStatus()
                                );

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        currentUser,
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                );
                                accessor.setUser(authentication);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("WebSocket STOMP CONNECT authentication rejected: {}", e.getMessage());
                    throw new ApiException(ErrorCode.UNAUTHORIZED, "STOMP connection authentication failed");
                }
            } else {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Missing STOMP authentication token");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (!(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof CurrentUser currentUser)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Authentication required to subscribe");
            }

            accountStatusGuard.checkActive(currentUser);

            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/conversations/")) {
                String convIdStr = destination.substring("/topic/conversations/".length());
                try {
                    UUID conversationId = UUID.fromString(convIdStr);
                    conversationAuthorizationService.requireCanView(currentUser.getUserId(), conversationId);
                } catch (IllegalArgumentException e) {
                    throw new ApiException(ErrorCode.BAD_REQUEST, "Invalid conversation ID in destination");
                }
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (!(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof CurrentUser currentUser)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Authentication required to send");
            }
            accountStatusGuard.checkActive(currentUser);
        }

        return message;
    }
}
