package com.rockranger.chat.notification.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.chat.notification.domain.EmailOutbox;
import com.rockranger.chat.notification.domain.EmailOutboxRepository;
import com.rockranger.chat.notification.domain.OutboxStatus;
import com.rockranger.chat.notification.infrastructure.BrevoEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class EmailOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(EmailOutboxWorker.class);
    private static final int MAX_ATTEMPTS = 5;

    private final EmailOutboxRepository outboxRepository;
    private final BrevoEmailSender emailSender;
    private final ObjectMapper objectMapper;

    public EmailOutboxWorker(EmailOutboxRepository outboxRepository,
                             BrevoEmailSender emailSender,
                             ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.emailSender = emailSender;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processOutbox() {
        List<EmailOutbox> pendingEmails = outboxRepository.findTop50ByStatusAndAttemptsLessThanOrderByCreatedAtAsc(
                OutboxStatus.PENDING, MAX_ATTEMPTS
        );

        if (pendingEmails.isEmpty()) {
            return;
        }

        for (EmailOutbox item : pendingEmails) {
            try {
                Map<String, String> payload = objectMapper.readValue(item.getPayload(), new TypeReference<>() {});
                String subject = payload.getOrDefault("subject", "Notification");
                String htmlContent = payload.getOrDefault("htmlContent", "");

                emailSender.sendEmail(item.getRecipient(), subject, htmlContent);

                item.setStatus(OutboxStatus.SENT);
                item.setSentAt(Instant.now());
                item.setLastError(null);
            } catch (Exception e) {
                int attempts = item.getAttempts() + 1;
                item.setAttempts(attempts);
                item.setLastError(e.getMessage());
                if (attempts >= MAX_ATTEMPTS) {
                    item.setStatus(OutboxStatus.DEAD_LETTER);
                    log.error("Outbox item {} marked as DEAD_LETTER after {} attempts: {}", item.getId(), attempts, e.getMessage());
                } else {
                    log.warn("Outbox item {} attempt {} failed: {}", item.getId(), attempts, e.getMessage());
                }
            }
            outboxRepository.save(item);
        }
    }
}
