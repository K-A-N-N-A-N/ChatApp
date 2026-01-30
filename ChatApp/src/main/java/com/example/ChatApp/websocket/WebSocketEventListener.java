package com.example.ChatApp.websocket;

import com.example.ChatApp.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserPresenceService userPresenceService;
    private final WebSocketSessionRegistry sessionRegistry;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        log.info("SESSION CONNECTED EVENT FIRED");

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        WebSocketSessionRegistry.SessionInfo info = sessionRegistry.get(sessionId);
        String userId = info != null ? info.getUserId() : null;
        String chatRoomId = info != null ? info.getChatRoomId() : null;

        log.info("sessionId: {}, userId from registry: {}, chatRoomId: {}", sessionId, userId, chatRoomId);

        if (userId == null) {
            log.warn(" userId is NULL in SessionConnectedEvent (sessionId={})", sessionId);
            return;
        }

        log.info("WebSocket CONNECT for userId={} (sessionId={})", userId, sessionId);
        userPresenceService.handleConnect(userId);
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        log.info("SESSION DISCONNECTED EVENT FIRED");

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        WebSocketSessionRegistry.SessionInfo info = sessionRegistry.remove(sessionId);
        String userId = info != null ? info.getUserId() : null;

        log.info("sessionId: {}, userId from registry: {}", sessionId, userId);

        if (userId == null) {
            log.warn("userId is NULL in SessionDisconnectEvent (sessionId={})", sessionId);
            return;
        }

        log.info("WebSocket DISCONNECT for userId={} (sessionId={})", userId, sessionId);
        userPresenceService.handleDisconnect(userId);
    }
}