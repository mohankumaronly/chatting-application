package com.rockranger.chat.session.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(UUID userId, Instant now);

    Optional<UserSession> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.revokedAt = :now WHERE s.userId = :userId AND s.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE UserSession s SET s.revokedAt = :now WHERE s.id = :sessionId AND s.revokedAt IS NULL")
    int revokeById(@Param("sessionId") UUID sessionId, @Param("now") Instant now);
}
