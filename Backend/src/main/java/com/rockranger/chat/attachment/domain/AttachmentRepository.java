package com.rockranger.chat.attachment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByOwnerId(UUID ownerId);

    @Query("SELECT a FROM Attachment a WHERE a.uploadStatus = :status AND a.createdAt < :cutoff")
    List<Attachment> findByUploadStatusAndCreatedAtBefore(
            @Param("status") UploadStatus status,
            @Param("cutoff") Instant cutoff
    );
}
