package com.example.air.alert;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    // WebSocket 핸들러를 등록하고 URL 경로를 매핑
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // NotificationWebSocketHandler를 "/ws/notifications" 경로에 매핑
        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notifications")
                .setAllowedOrigins("*"); // 모든 출처에서의 WebSocket 연결 허용
    }
}