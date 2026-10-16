package com.rockranger.chat.attachment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, MessageAttachmentId> {

    @Query("SELECT ma FROM MessageAttachment ma WHERE ma.id.messageId = :messageId ORDER BY ma.displayOrder ASC")
    List<MessageAttachment> findByMessageId(@Param("messageId") UUID messageId);

    @Query("SELECT ma FROM MessageAttachment ma WHERE ma.id.attachmentId = :attachmentId")
    List<MessageAttachment> findByAttachmentId(@Param("attachmentId") UUID attachmentId);

    @Query("SELECT COUNT(ma) > 0 FROM MessageAttachment ma WHERE ma.id.attachmentId = :attachmentId")
    boolean existsByAttachmentId(@Param("attachmentId") UUID attachmentId);
}
