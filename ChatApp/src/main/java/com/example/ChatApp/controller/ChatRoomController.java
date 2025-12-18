package com.example.ChatApp.controller;

import com.example.ChatApp.dto.ChatRoomResponse;
import com.example.ChatApp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/private/{userId}")
    public ResponseEntity<ChatRoomResponse> createPrivateChat(
            @PathVariable String userId,
            @RequestParam String currentUserId
    ) {
        return ResponseEntity.ok(
                chatRoomService.createPrivateChat(currentUserId, userId)
        );
    }
}

