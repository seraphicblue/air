package com.example.air.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*WebPushService는 알림 전송 로직을 캡슐화하여
  WebSocket을 통해 클라이언트에 알림을 전달하는 서비스*/
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {
    private final NotificationWebSocketHandler webSocketHandler;

    public void sendPushNotification(NotificationRequest request) {
        try {
            log.info("알림을 보냈습니다: {}", request);
            webSocketHandler.sendNotificationToAll(request);// WebSocket 핸들러를 통해 알림 전송
        } catch (Exception e) {
            throw new RuntimeException("알림 전송 실패", e);
        }
    }
}