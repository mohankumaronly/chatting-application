package com.rockranger.chat.notification.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.chat.common.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class BrevoEmailSender {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public BrevoEmailSender(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        String apiKey = appProperties.getBrevo().getApiKey();
        String senderEmail = appProperties.getBrevo().getSenderEmail();
        String senderName = appProperties.getBrevo().getSenderName();

        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("replace_")) {
            log.warn("Brevo API key not configured. Mocking email delivery to {}", toEmail);
            return;
        }

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_API_URL))
                .timeout(Duration.ofSeconds(15))
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("Email successfully sent via Brevo to recipient.");
        } else {
            log.error("Brevo API error: status={}, body={}", response.statusCode(), response.body());
            throw new RuntimeException("Brevo API responded with status " + response.statusCode());
        }
    }
}
