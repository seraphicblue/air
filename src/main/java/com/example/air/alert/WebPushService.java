package com.example.air.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {
    private final NotificationWebSocketHandler webSocketHandler;

    public void sendPushNotification(NotificationRequest request) {
        try {
            log.info("Sending notification: {}", request);
            webSocketHandler.sendNotificationToAll(request);
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            throw new RuntimeException("알림 전송 실패", e);
        }
    }
}