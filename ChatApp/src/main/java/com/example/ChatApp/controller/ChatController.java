package com.example.ChatApp.controller;

import com.example.ChatApp.dto.StartChatRequest;
import com.example.ChatApp.dto.StartChatResponse;
import com.example.ChatApp.dto.ChatRoomResponse;
import com.example.ChatApp.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/start")
    public StartChatResponse startChat(
            @RequestBody StartChatRequest request,
            HttpSession session
    ) {
        String currentUserId = (String) session.getAttribute("USER_ID");

        if (currentUserId == null) {
            throw new RuntimeException("User not logged in");
        }

        ChatRoomResponse room = chatRoomService
                .createPrivateChatByUsername(
                        currentUserId,
                        request.targetUsername()
                );

        return new StartChatResponse(
                room.getId(),
                room.getType(),
                room.getName()
        );
    }
}
