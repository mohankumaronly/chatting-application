package com.rockranger.chat.messaging.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MessageUserDeletionRepository extends JpaRepository<MessageUserDeletion, MessageUserDeletionId> {

    @Query("SELECT d.id.messageId FROM MessageUserDeletion d WHERE d.id.userId = :userId AND d.id.messageId IN :messageIds")
    Set<UUID> findDeletedMessageIdsForUser(@Param("userId") UUID userId, @Param("messageIds") Collection<UUID> messageIds);

    @Query("SELECT COUNT(d) > 0 FROM MessageUserDeletion d WHERE d.id.messageId = :messageId AND d.id.userId = :userId")
    boolean isMessageDeletedForUser(@Param("messageId") UUID messageId, @Param("userId") UUID userId);
}
