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
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendNotificationToAll(NotificationRequest notification) {
        String message = null;
        try {
            message = new ObjectMapper().writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            log.error("Error serializing notification", e);
            return;
        }

        String finalMessage = message;
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(finalMessage));
                }
            } catch (IOException e) {
                log.error("Error sending message to session", e);
            }
        });
    }
}