package com.rockranger.chat.realtime.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.chat.realtime.domain.OutboxStatus;
import com.rockranger.chat.realtime.domain.RealtimeOutbox;
import com.rockranger.chat.realtime.domain.RealtimeOutboxRepository;
import com.rockranger.chat.realtime.dto.RealtimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RealtimePublisher {

    private static final Logger log = LoggerFactory.getLogger(RealtimePublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final RealtimeOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public RealtimePublisher(
            SimpMessagingTemplate messagingTemplate,
            RealtimeOutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.messagingTemplate = messagingTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void publishToConversation(UUID conversationId, RealtimeEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, event);
        } catch (Exception e) {
            log.warn("Failed to publish event to /topic/conversations/{}: {}", conversationId, e.getMessage());
        }
    }

    public void publishToUser(UUID userId, RealtimeEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/events", event);
        } catch (Exception e) {
            log.warn("Failed to publish event to user {}: {}", userId, e.getMessage());
        }
    }

    public RealtimeOutbox recordOutboxEvent(String eventType, String aggregateType, UUID aggregateId, RealtimeEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            RealtimeOutbox outbox = new RealtimeOutbox(UUID.randomUUID(), eventType, aggregateType, aggregateId, json);
            return outboxRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to serialize outbox event: {}", eventType, e);
            return null;
        }
    }
}
