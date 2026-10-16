package com.rockranger.chat.conversation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {

    @Query("SELECT cm FROM ConversationMember cm WHERE cm.id.conversationId = :conversationId")
    List<ConversationMember> findByConversationId(@Param("conversationId") UUID conversationId);

    @Query("SELECT cm FROM ConversationMember cm WHERE cm.id.userId = :userId AND cm.leftAt IS NULL")
    List<ConversationMember> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT cm FROM ConversationMember cm WHERE cm.id.conversationId = :conversationId AND cm.id.userId = :userId")
    Optional<ConversationMember> findByConversationIdAndUserId(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(cm) > 0 FROM ConversationMember cm WHERE cm.id.conversationId = :conversationId AND cm.id.userId = :userId AND cm.leftAt IS NULL")
    boolean existsActive(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    @Query("SELECT cm.id.userId FROM ConversationMember cm WHERE cm.id.conversationId = :conversationId AND cm.id.userId != :userId AND cm.leftAt IS NULL")
    Set<UUID> findOtherActiveMemberIds(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
