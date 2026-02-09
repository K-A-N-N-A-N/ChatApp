package com.example.ChatApp.websocket;

import com.example.ChatApp.dto.*;
import com.example.ChatApp.entity.Message;
import com.example.ChatApp.entity.MessageStatusType;
import com.example.ChatApp.kafka.ChatMessageProducer;
import com.example.ChatApp.repository.MessageRepository;
import com.example.ChatApp.repository.UserRepository;
import com.example.ChatApp.service.MessageStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository chatUserRepository;
    private final MessageStatusService messageStatusService;
    private final ChatMessageProducer  chatMessageProducer;

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

        ChatMessageEvent event = new ChatMessageEvent(
                chatRoomId,
                userId,
                request.getContent(),
                Instant.now()
        );

        chatMessageProducer.publish(event);
    }


    @MessageMapping("/chat.markRead")
    public void markRead(
            MessageReadRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {

        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs == null) return;

        String userId = (String) sessionAttrs.get("userId");
        String chatRoomId = (String) sessionAttrs.get("chatRoomId");

        if (userId == null || chatRoomId == null) return;

        Message message = messageRepository.findById(request.getMessageId())
                .orElse(null);

        if (message == null) {
            return;
        }

        // Ensure the message belongs to the same room as the WebSocket session
        if (!chatRoomId.equals(message.getChatRoom().getId())) {
            return;
        }

        var status = messageStatusService.markAsRead(message.getId(), userId);

        long deliveredCount = messageStatusService.countByStatus(
                message.getId(),
                MessageStatusType.DELIVERED
        );

        long readCount = messageStatusService.countByStatus(
                message.getId(),
                MessageStatusType.READ
        );

        MessageReadReceipt receipt = new MessageReadReceipt(
                message.getId(),
                chatRoomId,
                userId,
                deliveredCount,
                readCount,
                status.getTimestamp()
        );

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId + "/receipts",
                receipt
        );
    }
}
