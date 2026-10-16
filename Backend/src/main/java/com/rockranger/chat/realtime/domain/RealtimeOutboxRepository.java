package com.rockranger.chat.realtime.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RealtimeOutboxRepository extends JpaRepository<RealtimeOutbox, UUID> {

    @Query("SELECT o FROM RealtimeOutbox o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<RealtimeOutbox> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status, Pageable pageable);
}
