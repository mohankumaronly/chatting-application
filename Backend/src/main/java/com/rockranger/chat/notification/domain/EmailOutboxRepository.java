package com.rockranger.chat.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, UUID> {
    List<EmailOutbox> findTop50ByStatusAndAttemptsLessThanOrderByCreatedAtAsc(OutboxStatus status, int maxAttempts);
}
