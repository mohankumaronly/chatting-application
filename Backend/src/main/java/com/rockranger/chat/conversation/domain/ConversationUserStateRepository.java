package com.rockranger.chat.conversation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationUserStateRepository extends JpaRepository<ConversationUserState, ConversationUserStateId> {

    @Query("SELECT s FROM ConversationUserState s WHERE s.id.conversationId = :conversationId AND s.id.userId = :userId")
    Optional<ConversationUserState> findByConversationIdAndUserId(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    @Query("SELECT s FROM ConversationUserState s WHERE s.id.userId = :userId AND s.hiddenAt IS NULL")
    List<ConversationUserState> findVisibleByUserId(@Param("userId") UUID userId);
}
