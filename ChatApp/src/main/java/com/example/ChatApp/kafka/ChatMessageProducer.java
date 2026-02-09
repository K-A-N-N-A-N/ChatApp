package com.example.ChatApp.kafka;

import com.example.ChatApp.dto.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

    public void publish(ChatMessageEvent event) {
        kafkaTemplate.send("chat-messages", event.getChatRoomId(), event);
    }
}
