package com.example.ChatApp.controller;

import com.example.ChatApp.dto.ChatMessageResponse;
import com.example.ChatApp.dto.ChatRoomResponse;
import com.example.ChatApp.dto.CreateGroupRequest;
import com.example.ChatApp.repository.MessageRepository;
import com.example.ChatApp.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/group")
    public ChatRoomResponse createGroup(
            @RequestBody CreateGroupRequest request,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) throw new RuntimeException("Not logged in");

        return chatRoomService.createGroupChat(userId, request.groupName());
    }

    @PostMapping("/group/{chatRoomId}/members")
    public void addGroupMember(
            @PathVariable String chatRoomId,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        String adminId = (String) session.getAttribute("USER_ID");
        if (adminId == null) throw new RuntimeException("Not logged in");

        chatRoomService.addMemberToGroup(
                adminId,
                chatRoomId,
                body.get("username")
        );
    }

    @DeleteMapping("/group/{chatRoomId}/members/{userId}")
    public void removeGroupMember(
            @PathVariable String chatRoomId,
            @PathVariable String userId,
            HttpSession session
    ) {
        String adminUserId = (String) session.getAttribute("USER_ID");
        if (adminUserId == null) {
            throw new RuntimeException("Not logged in");
        }

        chatRoomService.removeMemberFromGroup(
                adminUserId,
                chatRoomId,
                userId
        );
    }
}

