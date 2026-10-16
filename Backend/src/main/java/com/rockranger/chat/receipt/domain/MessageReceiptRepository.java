package com.rockranger.chat.receipt.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, MessageReceiptId> {

    @Query("SELECT r FROM MessageReceipt r WHERE r.id.messageId = :messageId")
    List<MessageReceipt> findByMessageId(@Param("messageId") UUID messageId);

    @Query("SELECT r FROM MessageReceipt r WHERE r.id.messageId = :messageId AND r.id.userId = :userId")
    Optional<MessageReceipt> findByMessageIdAndUserId(@Param("messageId") UUID messageId, @Param("userId") UUID userId);
}
