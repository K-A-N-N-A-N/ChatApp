package com.example.ChatApp.websocket;

import com.example.ChatApp.repository.ChatRoomMemberRepository;
import com.example.ChatApp.repository.ChatRoomRepository;
import com.example.ChatApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        URI uri = request.getURI();
        if (uri.getQuery() == null) return false;

        Map<String, String> params = new HashMap<>();
        for (String pair : uri.getQuery().split("&")) {
            String[] kv = pair.split("=");
            params.put(kv[0], kv[1]);
        }

        String userId = params.get("userId");
        String chatRoomId = params.get("chatRoomId");

        if (userId == null || chatRoomId == null) return false;

        // validate user
        if (userRepository.findByIdAndActiveTrue(userId).isEmpty()) return false;

        // validate room
        if (!chatRoomRepository.existsById(chatRoomId)) return false;

        // validate membership
        if (!memberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId))
            return false;

        attributes.put("userId", userId);
        attributes.put("chatRoomId", chatRoomId);

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}
