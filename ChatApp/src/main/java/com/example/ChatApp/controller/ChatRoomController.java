package com.example.ChatApp.controller;

import com.example.ChatApp.dto.*;
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

    @GetMapping("/listMyChatRooms")
    public List<ChatRoomListResponse> listMyChatRooms(HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new RuntimeException("Not logged in");
        }
        return chatRoomService.listMyChatRooms(userId);
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
    public ApiResponse addGroupMember(
            @PathVariable String chatRoomId,
            @RequestBody AddMemberRequest request,
            HttpSession session
    ) {
        String adminUserId = (String) session.getAttribute("USER_ID");
        if (adminUserId == null) {
            throw new RuntimeException("Not logged in");
        }

        chatRoomService.addMemberToGroup(
                adminUserId,
                chatRoomId,
                request.getUsername()
        );

        return new ApiResponse("User added to group successfully");
    }

    @DeleteMapping("/group/{chatRoomId}/members")
    public ApiResponse removeGroupMember(
            @PathVariable String chatRoomId,
            @RequestBody RemoveMemberRequest request,
            HttpSession session
    ) {
        String adminUserId = (String) session.getAttribute("USER_ID");
        if (adminUserId == null) {
            throw new RuntimeException("Not logged in");
        }

        chatRoomService.removeMemberFromGroup(
                adminUserId,
                chatRoomId,
                request.getUsername()
        );

        return new ApiResponse("User removed from group successfully");
    }
}

