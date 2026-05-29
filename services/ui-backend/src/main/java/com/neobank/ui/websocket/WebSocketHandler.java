package com.neobank.ui.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.ui.stats.StatsAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final StatsAggregator statsAggregator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("UI client connected: {}", session.getId());
        log.info("Total active connections: {}", sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("UI client disconnected: {}, Status: {}", session.getId(), status);
        log.info("Total active connections: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received message from client {}: {}", session.getId(), message.getPayload());
        // Echo back for testing
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void broadcastStats() {
        if (sessions.isEmpty()) {
            log.debug("No active WebSocket sessions to broadcast");
            return;
        }

        try {
            String statsJson = statsAggregator.getCurrentStatsJson();
            TextMessage message = new TextMessage(statsJson);

            // Remove closed sessions and broadcast to active ones
            sessions.values().removeIf(session -> !session.isOpen());

            for (WebSocketSession session : sessions.values()) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (Exception e) {
                    log.error("Failed to send message to session: {}", session.getId(), e);
                    sessions.remove(session.getId());
                }
            }
            log.debug("Broadcasted stats to {} clients", sessions.size());
        } catch (Exception e) {
            log.error("Failed to broadcast stats", e);
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}