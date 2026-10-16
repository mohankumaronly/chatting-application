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

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.senderId != :userId AND (:lastReadAt IS NULL OR m.createdAt > :lastReadAt)")
    long countUnreadMessages(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("lastReadAt") Instant lastReadAt
    );

    Optional<Message> findFirstByConversationIdOrderBySequenceNumberDesc(UUID conversationId);
}
