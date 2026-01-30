package com.example.ChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class MessageReadReceipt {

    private String messageId;
    private String chatRoomId;
    private String readerId;
    private long deliveredCount;
    private long readCount;
    private Instant readAt;
}

