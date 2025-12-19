package com.example.ChatApp.websocket;

import com.example.ChatApp.dto.ChatMessageRequest;
import com.example.ChatApp.dto.ChatMessageResponse;
import com.example.ChatApp.entity.ChatRoom;
import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.entity.Message;
import com.example.ChatApp.entity.MessageType;
import com.example.ChatApp.repository.MessageRepository;
import com.example.ChatApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository chatUserRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(
            ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {

        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs == null) return;

        String userId = (String) sessionAttrs.get("userId");
        String chatRoomId = (String) sessionAttrs.get("chatRoomId");

        if (userId == null || chatRoomId == null) return;
        if (request.getContent() == null || request.getContent().isBlank()) return;

        // Fetch username
        ChatUser sender = chatUserRepository.findById(userId)
                .orElseThrow();

        // ID-only reference for room
        ChatRoom roomRef = new ChatRoom();
        roomRef.setId(chatRoomId);

        Message message = new Message();
        message.setChatRoom(roomRef);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(MessageType.TEXT);

        messageRepository.save(message);

        ChatMessageResponse response = new ChatMessageResponse(
                sender.getId(),
                sender.getUsername(),
                message.getContent(),
                message.getCreatedAt()
        );

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                response
        );
    }
}
