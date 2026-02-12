package com.example.ChatApp.service;

import com.example.ChatApp.dto.UserPresenceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // userId → active WebSocket connection count
    private final Map<String, Integer> activeConnections = new ConcurrentHashMap<>();

    public void userConnected(String userId) {
        activeConnections.merge(userId, 1, Integer::sum);

        // first connection → ONLINE
        if (activeConnections.get(userId) == 1) {
            broadcast(userId, true);
        }
    }

    public void userDisconnected(String userId) {
        activeConnections.computeIfPresent(userId, (k, v) -> v > 1 ? v - 1 : null);

        // no connections left → OFFLINE
        if (!activeConnections.containsKey(userId)) {
            broadcast(userId, false);
        }
    }

    public boolean isOnline(String userId) {
        return activeConnections.containsKey(userId);
    }

    /**
     * Broadcast a snapshot of all currently online users.
     * This is used by clients after they subscribe so they don't miss
     * ONLINE events that happened before their subscription was active.
     */
    public void broadcastSnapshot() {
        activeConnections.keySet().forEach(userId -> broadcast(userId, true));
    }

    private void broadcast(String userId, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/presence",
                new UserPresenceEvent(userId, online)
        );
    }
}
