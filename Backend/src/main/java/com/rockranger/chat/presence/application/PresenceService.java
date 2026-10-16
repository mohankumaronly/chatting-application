package com.rockranger.chat.presence.application;

import com.rockranger.chat.block.application.BlockPolicy;
import com.rockranger.chat.presence.domain.UserPresenceSettings;
import com.rockranger.chat.presence.domain.UserPresenceSettingsRepository;
import com.rockranger.chat.presence.domain.VisibilitySetting;
import com.rockranger.chat.presence.dto.PresenceResponse;
import com.rockranger.chat.presence.dto.UpdatePrivacySettingsRequest;
import com.rockranger.chat.presence.dto.UserPresenceSettingsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);

    private final StringRedisTemplate redisTemplate;
    private final UserPresenceSettingsRepository settingsRepository;
    private final BlockPolicy blockPolicy;

    public PresenceService(
            StringRedisTemplate redisTemplate,
            UserPresenceSettingsRepository settingsRepository,
            BlockPolicy blockPolicy
    ) {
        this.redisTemplate = redisTemplate;
        this.settingsRepository = settingsRepository;
        this.blockPolicy = blockPolicy;
    }

    public void markSessionActive(UUID userId, UUID sessionId) {
        try {
            String sessionKey = "presence:user:" + userId + ":session:" + sessionId;
            String setKey = "presence:user:" + userId + ":sessions";
            redisTemplate.opsForValue().set(sessionKey, "1", Duration.ofSeconds(60));
            redisTemplate.opsForSet().add(setKey, sessionId.toString());
            redisTemplate.expire(setKey, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.warn("Redis error marking session active for user {}", userId, e);
        }
    }

    public void heartbeat(UUID userId, UUID sessionId) {
        try {
            String sessionKey = "presence:user:" + userId + ":session:" + sessionId;
            redisTemplate.opsForValue().set(sessionKey, "1", Duration.ofSeconds(60));
        } catch (Exception e) {
            log.warn("Redis error on heartbeat for user {}", userId, e);
        }
    }

    public void markSessionInactive(UUID userId, UUID sessionId) {
        try {
            String sessionKey = "presence:user:" + userId + ":session:" + sessionId;
            String setKey = "presence:user:" + userId + ":sessions";
            redisTemplate.delete(sessionKey);
            redisTemplate.opsForSet().remove(setKey, sessionId.toString());

            if (getActiveSessionCount(userId) == 0) {
                recordOffline(userId, Instant.now());
            }
        } catch (Exception e) {
            log.warn("Redis error marking session inactive for user {}", userId, e);
        }
    }

    public int getActiveSessionCount(UUID userId) {
        try {
            String setKey = "presence:user:" + userId + ":sessions";
            Set<String> sessions = redisTemplate.opsForSet().members(setKey);
            if (sessions == null || sessions.isEmpty()) return 0;

            int active = 0;
            for (String sid : sessions) {
                String sessionKey = "presence:user:" + userId + ":session:" + sid;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                    active++;
                } else {
                    redisTemplate.opsForSet().remove(setKey, sid);
                }
            }
            return active;
        } catch (Exception e) {
            log.warn("Redis error checking active session count for user {}", userId, e);
            return 0;
        }
    }

    public void recordOffline(UUID userId, Instant lastSeen) {
        try {
            String lastSeenKey = "presence:user:" + userId + ":last_seen";
            redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(lastSeen.toEpochMilli()));
        } catch (Exception e) {
            log.warn("Redis error recording offline time for user {}", userId, e);
        }
    }

    public Instant getLastSeen(UUID userId) {
        try {
            String lastSeenKey = "presence:user:" + userId + ":last_seen";
            String val = redisTemplate.opsForValue().get(lastSeenKey);
            if (val != null) {
                return Instant.ofEpochMilli(Long.parseLong(val));
            }
        } catch (Exception e) {
            log.warn("Redis error fetching last seen for user {}", userId, e);
        }
        return null;
    }

    public PresenceResponse getPresence(UUID viewerId, UUID targetId) {
        if (blockPolicy.hasAnyBlock(viewerId, targetId)) {
            return PresenceResponse.hidden(targetId);
        }

        UserPresenceSettings settings = settingsRepository.findById(targetId)
                .orElseGet(() -> UserPresenceSettings.defaults(targetId));

        boolean canSeeOnline = settings.getOnlineVisibility() != VisibilitySetting.NOBODY;
        boolean canSeeLastSeen = settings.getLastSeenVisibility() != VisibilitySetting.NOBODY;

        int activeSessions = getActiveSessionCount(targetId);
        boolean isOnline = canSeeOnline && (activeSessions > 0);
        Instant lastSeen = (canSeeLastSeen && !isOnline) ? getLastSeen(targetId) : null;

        return new PresenceResponse(targetId, isOnline, lastSeen);
    }

    @Transactional(readOnly = true)
    public UserPresenceSettingsResponse getPrivacySettings(UUID userId) {
        UserPresenceSettings settings = settingsRepository.findById(userId)
                .orElseGet(() -> UserPresenceSettings.defaults(userId));
        return toSettingsResponse(settings);
    }

    @Transactional
    public UserPresenceSettingsResponse updatePrivacySettings(UUID userId, UpdatePrivacySettingsRequest req) {
        UserPresenceSettings settings = settingsRepository.findById(userId)
                .orElseGet(() -> new UserPresenceSettings(userId));

        if (req.lastSeenVisibility() != null) {
            settings.setLastSeenVisibility(req.lastSeenVisibility());
        }
        if (req.onlineVisibility() != null) {
            settings.setOnlineVisibility(req.onlineVisibility());
        }
        if (req.readReceiptsEnabled() != null) {
            settings.setReadReceiptsEnabled(req.readReceiptsEnabled());
        }
        if (req.typingVisibility() != null) {
            settings.setTypingVisibility(req.typingVisibility());
        }
        if (req.profilePhotoVisibility() != null) {
            settings.setProfilePhotoVisibility(req.profilePhotoVisibility());
        }
        settings.setUpdatedAt(Instant.now());
        settingsRepository.save(settings);

        return toSettingsResponse(settings);
    }

    private UserPresenceSettingsResponse toSettingsResponse(UserPresenceSettings s) {
        return new UserPresenceSettingsResponse(
                s.getUserId(),
                s.getLastSeenVisibility().name(),
                s.getOnlineVisibility().name(),
                s.isReadReceiptsEnabled(),
                s.isTypingVisibility(),
                s.getProfilePhotoVisibility().name(),
                s.getUpdatedAt()
        );
    }
}
