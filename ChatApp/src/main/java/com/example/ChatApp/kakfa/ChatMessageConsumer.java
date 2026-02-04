package com.example.ChatApp.kakfa;

import com.example.ChatApp.dto.ChatMessageEvent;
import com.example.ChatApp.dto.ChatMessageResponse;
import com.example.ChatApp.entity.*;
import com.example.ChatApp.repository.MessageRepository;
import com.example.ChatApp.repository.UserRepository;
import com.example.ChatApp.service.MessageStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final MessageRepository messageRepository;
    private final UserRepository chatUserRepository;
    private final MessageStatusService messageStatusService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "chat-messages", groupId = "chat-message-group")
    public void consume(ChatMessageEvent event) {

        ChatUser sender = chatUserRepository.findById(event.getSenderId())
                .orElseThrow();

        ChatRoom roomRef = new ChatRoom();
        roomRef.setId(event.getChatRoomId());

        Message message = new Message();
        message.setChatRoom(roomRef);
        message.setSender(sender);
        message.setContent(event.getContent());
        message.setMessageType(MessageType.TEXT);

        messageRepository.save(message);

        // Create delivered statuses
        messageStatusService.createDeliveredStatusesForMessage(message);

        long deliveredCount = messageStatusService.countByStatus(
                message.getId(),
                MessageStatusType.DELIVERED
        );

        long readCount = messageStatusService.countByStatus(
                message.getId(),
                MessageStatusType.READ
        );

        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                sender.getId(),
                sender.getUsername(),
                message.getContent(),
                message.getCreatedAt(),
                deliveredCount,
                readCount
        );

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + event.getChatRoomId(),
                response
        );
    }
}


