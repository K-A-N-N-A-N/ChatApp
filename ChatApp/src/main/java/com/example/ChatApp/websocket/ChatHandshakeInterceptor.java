package com.example.ChatApp.websocket;

import com.example.ChatApp.repository.ChatRoomMemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final ChatRoomMemberRepository memberRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        // Get HTTP session
        HttpSession session = servletRequest.getServletRequest().getSession(false);
        if (session == null) return false;

        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) return false;

        // chatRoomId still comes from URL
        String chatRoomId =
                servletRequest.getServletRequest().getParameter("chatRoomId");

        if (chatRoomId == null) return false;

        // Validate membership
        boolean isMember =
                memberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);

        if (!isMember) return false;

        // Store into WebSocket session
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
