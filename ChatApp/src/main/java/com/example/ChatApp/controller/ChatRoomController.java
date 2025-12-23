package com.example.ChatApp.controller;

import com.example.ChatApp.dto.ChatMessageResponse;
import com.example.ChatApp.dto.ChatRoomResponse;
import com.example.ChatApp.repository.MessageRepository;
import com.example.ChatApp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MessageRepository messageRepository;

    @PostMapping("/private/{userId}")
    public ResponseEntity<ChatRoomResponse> createPrivateChat(
            @PathVariable String userId,
            @RequestParam String currentUserId
    ) {
        return ResponseEntity.ok(
                chatRoomService.createPrivateChat(currentUserId, userId)
        );
    }

    @GetMapping("/{chatRoomId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable String chatRoomId) {

        return messageRepository
                .findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
                .stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getSender().getId(),
                        msg.getSender().getUsername(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }

}

