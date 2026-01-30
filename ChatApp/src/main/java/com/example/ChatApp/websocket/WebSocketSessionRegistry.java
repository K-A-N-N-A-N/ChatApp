package com.example.ChatApp.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    @Data
    @AllArgsConstructor
    public static class SessionInfo {
        private String userId;
        private String chatRoomId;
    }

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void register(String sessionId, String userId, String chatRoomId) {
        if (sessionId == null || userId == null) {
            return;
        }
        sessions.put(sessionId, new SessionInfo(userId, chatRoomId));
    }

    public SessionInfo get(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessions.get(sessionId);
    }

    public SessionInfo remove(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessions.remove(sessionId);
    }
}

