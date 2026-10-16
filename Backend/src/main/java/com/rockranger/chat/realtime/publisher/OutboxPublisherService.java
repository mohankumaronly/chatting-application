package com.rockranger.chat.realtime.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.chat.realtime.domain.OutboxStatus;
import com.rockranger.chat.realtime.domain.RealtimeOutbox;
import com.rockranger.chat.realtime.domain.RealtimeOutboxRepository;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherService.class);

    private final RealtimeOutboxRepository outboxRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisherService(
            RealtimeOutboxRepository outboxRepository,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingOutboxEvents() {
        List<RealtimeOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, 50)
        );

        for (RealtimeOutbox outbox : pending) {
            try {
                RealtimeEvent event = objectMapper.readValue(outbox.getPayload(), RealtimeEvent.class);
                if (event.conversationId() != null) {
                    messagingTemplate.convertAndSend("/topic/conversations/" + event.conversationId(), event);
                }
                outbox.setStatus(OutboxStatus.PUBLISHED);
                outbox.setPublishedAt(Instant.now());
            } catch (Exception e) {
                log.warn("Failed to publish outbox event {}: {}", outbox.getId(), e.getMessage());
                outbox.setAttempts(outbox.getAttempts() + 1);
                outbox.setLastError(e.getMessage());
                if (outbox.getAttempts() >= 5) {
                    outbox.setStatus(OutboxStatus.FAILED);
                }
            }
            outboxRepository.save(outbox);
        }
    }
}
