package com.example.ChatApp.config;

import com.example.ChatApp.websocket.ChatHandshakeInterceptor;
import com.example.ChatApp.websocket.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatHandshakeInterceptor chatHandshakeInterceptor;
    private final WebSocketSessionRegistry sessionRegistry;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Get the session attributes from the handshake
                    Map<String, Object> sessionAttrs = accessor.getSessionAttributes();

                    if (sessionAttrs != null) {
                        String userId = (String) sessionAttrs.get("userId");
                        String chatRoomId = (String) sessionAttrs.get("chatRoomId");

                        if (userId != null && chatRoomId != null) {
                            // Store mapping from WebSocket sessionId -> (userId, chatRoomId)
                            String sessionId = accessor.getSessionId();
                            sessionRegistry.register(sessionId, userId, chatRoomId);

                            // Store in NATIVE headers (not just message headers)
                            accessor.setNativeHeader("userId", userId);
                            accessor.setNativeHeader("chatRoomId", chatRoomId);

                            // Also set in regular headers for easy access
                            accessor.setHeader("userId", userId);
                            accessor.setHeader("chatRoomId", chatRoomId);

                            System.out.println("STOMP CONNECT - set headers - userId: " + userId + ", chatRoomId: " + chatRoomId + ", sessionId: " + sessionId);
                        } else {
                            System.out.println("userId or chatRoomId is NULL in session attributes");
                        }
                    } else {
                        System.out.println("sessionAttrs is NULL in CONNECT command");
                    }
                }

                return message;
            }
        });
    }
}