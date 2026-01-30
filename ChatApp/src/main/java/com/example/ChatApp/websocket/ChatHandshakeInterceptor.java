package com.example.ChatApp.websocket;

import com.example.ChatApp.repository.ChatRoomMemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
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
            log.warn("Not a ServletServerHttpRequest");
            return false;
        }

        // Get HTTP session
        HttpSession session = servletRequest.getServletRequest().getSession(false);
        if (session == null) {
            log.warn("No HTTP session found");
            return false;
        }

        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            log.warn("No USER_ID in session");
            return false;
        }

        // chatRoomId comes from URL
        String chatRoomId =
                servletRequest.getServletRequest().getParameter("chatRoomId");

        if (chatRoomId == null) {
            log.warn("No chatRoomId parameter");
            return false;
        }

        // Validate membership
        boolean isMember =
                memberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);

        if (!isMember) {
            log.warn("User {} not a member of room {}", userId, chatRoomId);
            return false;
        }

        // Store into WebSocket session
        attributes.put("userId", userId);
        attributes.put("chatRoomId", chatRoomId);

        log.info("WebSocket handshake SUCCESS - userId: {}, chatRoomId: {}", userId, chatRoomId);

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            log.error("Handshake error", exception);
        }
    }
}