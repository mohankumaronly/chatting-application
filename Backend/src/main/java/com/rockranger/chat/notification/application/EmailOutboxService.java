package com.rockranger.chat.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.chat.notification.domain.EmailOutbox;
import com.rockranger.chat.notification.domain.EmailOutboxRepository;
import com.rockranger.chat.notification.infrastructure.BrevoEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class EmailOutboxService {

    private static final Logger log = LoggerFactory.getLogger(EmailOutboxService.class);

    private final EmailOutboxRepository outboxRepository;
    private final BrevoEmailSender emailSender;
    private final ObjectMapper objectMapper;

    public EmailOutboxService(EmailOutboxRepository outboxRepository,
                              BrevoEmailSender emailSender,
                              ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.emailSender = emailSender;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void enqueueEmail(String recipient, String template, String subject, String htmlContent) {
        try {
            Map<String, String> payloadMap = Map.of(
                    "subject", subject,
                    "htmlContent", htmlContent
            );
            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            EmailOutbox event = new EmailOutbox(UUID.randomUUID(), recipient, template, payloadJson);
            outboxRepository.save(event);
            log.info("Enqueued email outbox event {} for recipient", event.getId());
        } catch (Exception e) {
            log.error("Failed to enqueue email outbox event: {}", e.getMessage(), e);
        }
    }
}
