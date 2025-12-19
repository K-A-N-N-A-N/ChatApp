package com.example.ChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private String senderId;
    private String senderUsername;
    private String content;
    private Instant createdAt;
}
