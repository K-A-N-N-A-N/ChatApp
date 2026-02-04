package com.example.ChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEvent {
    private String chatRoomId;
    private String senderId;
    private String content;
    private Instant timestamp;
}
