package com.example.ChatApp.websocket;

import com.example.ChatApp.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PresenceWebSocketController {

    private final PresenceService presenceService;

    /**
     * Client-initiated sync: when a WebSocket session is fully connected
     * and has subscribed to /topic/presence, the client can send a message
     * to /app/presence.sync to request a full snapshot of current online users.
     *
     * The snapshot is broadcast as a series of UserPresenceEvent(userId, true)
     * messages on /topic/presence.
     */
    @MessageMapping("/presence.sync")
    public void syncPresence() {
        presenceService.broadcastSnapshot();
    }
}

