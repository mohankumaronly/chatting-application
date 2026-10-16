package com.rockranger.chat.session.application;

import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.util.HmacUtil;
import com.rockranger.chat.common.util.SecureRandomUtil;
import com.rockranger.chat.session.domain.ClientType;
import com.rockranger.chat.session.domain.UserSession;
import com.rockranger.chat.session.domain.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final AppProperties appProperties;

    public SessionService(UserSessionRepository sessionRepository, AppProperties appProperties) {
        this.sessionRepository = sessionRepository;
        this.appProperties = appProperties;
    }

    public record SessionCreationResult(UserSession session, String rawRefreshToken) {}

    @Transactional
    public SessionCreationResult createSession(UUID userId, String deviceName, ClientType clientType, boolean keepLoggedIn) {
        long ttlSeconds = keepLoggedIn ?
                appProperties.getRefreshToken().getRememberTtlSeconds() :
                appProperties.getRefreshToken().getTtlSeconds();

        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        String rawRefreshToken = SecureRandomUtil.generateRandomToken(32);
        String tokenHash = HmacUtil.computeSha256Hex(rawRefreshToken);

        UUID sessionId = UUID.randomUUID();
        UserSession session = new UserSession(sessionId, userId, tokenHash, deviceName, clientType, expiresAt);
        sessionRepository.save(session);

        return new SessionCreationResult(session, rawRefreshToken);
    }

    @Transactional
    public Optional<SessionCreationResult> rotateSession(String rawRefreshToken, String deviceName) {
        String tokenHash = HmacUtil.computeSha256Hex(rawRefreshToken);
        Optional<UserSession> sessionOpt = sessionRepository.findByRefreshTokenHash(tokenHash);

        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        UserSession currentSession = sessionOpt.get();
        if (!currentSession.isActive()) {
            // Replay detection: If token was already revoked, revoke family / all sessions
            sessionRepository.revokeAllByUserId(currentSession.getUserId(), Instant.now());
            return Optional.empty();
        }

        Instant now = Instant.now();
        currentSession.setRevokedAt(now);

        long ttlSeconds = appProperties.getRefreshToken().getTtlSeconds();
        Instant newExpiresAt = now.plusSeconds(ttlSeconds);
        String newRawToken = SecureRandomUtil.generateRandomToken(32);
        String newTokenHash = HmacUtil.computeSha256Hex(newRawToken);

        UUID newSessionId = UUID.randomUUID();
        UserSession newSession = new UserSession(
                newSessionId,
                currentSession.getUserId(),
                newTokenHash,
                deviceName != null ? deviceName : currentSession.getDeviceName(),
                currentSession.getClientType(),
                newExpiresAt
        );

        currentSession.setReplacedBySessionId(newSessionId);
        sessionRepository.save(currentSession);
        sessionRepository.save(newSession);

        return Optional.of(new SessionCreationResult(newSession, newRawToken));
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.revokeById(sessionId, Instant.now());
    }

    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        sessionRepository.revokeAllByUserId(userId, Instant.now());
    }

    public List<UserSession> getActiveSessions(UUID userId) {
        return sessionRepository.findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(userId, Instant.now());
    }
}
