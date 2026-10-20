package com.rockranger.chat.realtime.config;

import com.rockranger.chat.common.config.AppProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AppProperties appProperties;
    private final AuthChannelInterceptor authChannelInterceptor;

    public WebSocketConfig(AppProperties appProperties, AuthChannelInterceptor authChannelInterceptor) {
        this.appProperties = appProperties;
        this.authChannelInterceptor = authChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String allowedOriginsStr = appProperties.getCors().getAllowedOrigins();
        String[] allowedOrigins = allowedOriginsStr != null && !allowedOriginsStr.isBlank()
                ? allowedOriginsStr.split(",")
                : new String[]{"http://localhost:5173"};

        // 1. Pure WebSocket endpoint for native clients (Frontend, Hoppscotch, Postman)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins);

        // 2. SockJS fallback endpoint (if you need it for older browsers)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}