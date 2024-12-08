package com.example.air.alert;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//연결된 모든 세션에 알림 메시지를 전송함
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    // 활성화된 WebSocket 세션을 저장하는 스레드 안전한 리스트
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /*새로운 WebSocket 연결이 수립되었을 때 호출
    세션을 리스트에 추가하여 이후 알림 전송 시 사용*/
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    /* WebSocket 연결이 종료되었을 때 호출
      세션을 리스트에서 제거하여 더 이상 알림을 전송하지 않음*/
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    // 연결된 모든 WebSocket 세션에 알림을 전송
    public void sendNotificationToAll(NotificationRequest notification) {
        String message = null;

        // 알림 객체를 JSON 형식으로 변환
        try {
            message = new ObjectMapper().writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            log.error("Error serializing notification", e);
            return;
        }

        // JSON 메시지를 각 WebSocket 세션에 전송
        String finalMessage = message;
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {// 세션이 열려 있는 경우에만 메시지 전송
                    session.sendMessage(new TextMessage(finalMessage));
                }
            } catch (IOException e) {
                log.error("Error sending message to session", e);
            }
        });
    }
}