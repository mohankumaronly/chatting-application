package com.rockranger.chat.presence.application;

import com.rockranger.chat.conversation.application.ConversationAuthorizationService;
import com.rockranger.chat.presence.domain.UserPresenceSettings;
import com.rockranger.chat.presence.domain.UserPresenceSettingsRepository;
import com.rockranger.chat.rate_limit.RateLimitService;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import com.rockranger.chat.realtime.publisher.RealtimePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class TypingService {

    private static final Logger log = LoggerFactory.getLogger(TypingService.class);

    private final StringRedisTemplate redisTemplate;
    private final ConversationAuthorizationService authorizationService;
    private final UserPresenceSettingsRepository settingsRepository;
    private final RealtimePublisher realtimePublisher;
    private final RateLimitService rateLimitService;

    public TypingService(
            StringRedisTemplate redisTemplate,
            ConversationAuthorizationService authorizationService,
            UserPresenceSettingsRepository settingsRepository,
            RealtimePublisher realtimePublisher,
            RateLimitService rateLimitService
    ) {
        this.redisTemplate = redisTemplate;
        this.authorizationService = authorizationService;
        this.settingsRepository = settingsRepository;
        this.realtimePublisher = realtimePublisher;
        this.rateLimitService = rateLimitService;
    }

    public void startTyping(UUID userId, UUID conversationId) {
        rateLimitService.enforce("chat:rl:typing:" + userId, 30, Duration.ofMinutes(1));
        authorizationService.requireCanSend(userId, conversationId);

        UserPresenceSettings settings = settingsRepository.findById(userId)
                .orElseGet(() -> UserPresenceSettings.defaults(userId));

        if (!settings.isTypingVisibility()) {
            return;
        }

        try {
            String key = "typing:" + conversationId + ":" + userId;
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(5));
        } catch (Exception e) {
            log.warn("Redis error on typing start for user {}", userId, e);
        }

        RealtimeEvent event = RealtimeEvent.of(
                "typing.started",
                conversationId,
                Map.of("conversationId", conversationId, "userId", userId)
        );
        realtimePublisher.publishToConversation(conversationId, event);
    }

    public void stopTyping(UUID userId, UUID conversationId) {
        authorizationService.requireMembership(userId, conversationId);

        try {
            String key = "typing:" + conversationId + ":" + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis error on typing stop for user {}", userId, e);
        }

        RealtimeEvent event = RealtimeEvent.of(
                "typing.stopped",
                conversationId,
                Map.of("conversationId", conversationId, "userId", userId)
        );
        realtimePublisher.publishToConversation(conversationId, event);
    }
}
