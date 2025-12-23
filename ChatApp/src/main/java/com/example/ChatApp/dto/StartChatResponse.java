package com.example.ChatApp.dto;

public record StartChatResponse(
        String chatRoomId,
        String chatType,
        String chatName
) {}
