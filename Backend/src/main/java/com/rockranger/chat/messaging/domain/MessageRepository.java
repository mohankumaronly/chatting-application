package com.rockranger.chat.messaging.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Optional<Message> findBySenderIdAndClientMessageId(UUID senderId, UUID clientMessageId);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.sequenceNumber DESC")
    List<Message> findLatestByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.sequenceNumber < :beforeSequence ORDER BY m.sequenceNumber DESC")
    List<Message> findByConversationIdBeforeSequence(
            @Param("conversationId") UUID conversationId,
            @Param("beforeSequence") long beforeSequence,
            Pageable pageable
    );

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.senderId != :userId")
    long countUnreadMessagesAll(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.senderId != :userId AND m.createdAt > :lastReadAt")
    long countUnreadMessagesSince(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("lastReadAt") Instant lastReadAt
    );

    /**
     * Counts messages in a conversation that were not sent by the given user
     * and were created after the user's last-read timestamp.
     *
     * <p>Implemented as a default method that dispatches to one of two explicit
     * queries, instead of a single query using {@code (:lastReadAt IS NULL OR ...)}.
     * The {@code ? IS NULL} pattern causes PostgreSQL to fail with
     * "could not determine data type of parameter $N" when the JDBC driver
     * binds an untyped {@code null}. Splitting into two queries avoids this
     * entirely and lets each SQL statement use the appropriate index.</p>
     */
    default long countUnreadMessages(UUID conversationId, UUID userId, Instant lastReadAt) {
        return lastReadAt == null
                ? countUnreadMessagesAll(conversationId, userId)
                : countUnreadMessagesSince(conversationId, userId, lastReadAt);
    }

    Optional<Message> findFirstByConversationIdOrderBySequenceNumberDesc(UUID conversationId);
}