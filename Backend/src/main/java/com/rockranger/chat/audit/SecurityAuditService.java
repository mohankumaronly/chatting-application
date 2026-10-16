package com.rockranger.chat.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);

    public void logEvent(String eventType, UUID userId, String detail) {
        log.info("[SECURITY_AUDIT] event={} userId={} timestamp={} detail={}",
                eventType, userId != null ? userId : "ANONYMOUS", Instant.now(), detail);
    }
}
