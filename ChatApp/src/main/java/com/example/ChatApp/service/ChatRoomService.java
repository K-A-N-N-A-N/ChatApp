package com.example.ChatApp.service;

import com.example.ChatApp.dto.ChatRoomResponse;
import com.example.ChatApp.entity.*;
import com.example.ChatApp.repository.ChatRoomMemberRepository;
import com.example.ChatApp.repository.ChatRoomRepository;
import com.example.ChatApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ChatRoomResponse createPrivateChat(String currentUserId, String targetUserId) {

        // self chat not allowed
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot create private chat with yourself");
        }

        ChatUser currentUser = userRepository.findByIdAndActiveTrue(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found or inactive"));

        ChatUser targetUser = userRepository.findByIdAndActiveTrue(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found or inactive"));

        // Check existing private chat
        List<String> roomIds = memberRepository.findExistingPrivateRoom(
                currentUserId,
                targetUserId,
                ChatRoomType.PRIVATE
        );

        if (!roomIds.isEmpty()) {
            ChatRoom existingRoom = chatRoomRepository.findById(roomIds.get(0))
                    .orElseThrow();

            log.info(
                    "Connected to EXISTING PRIVATE chat | roomId={} | users=[{}, {}]",
                    existingRoom.getId(),
                    currentUser.getUsername(),
                    targetUser.getUsername()
            );

            return mapToResponse(existingRoom);
        }

        // Create new private chat room
        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomType.PRIVATE);
        room.setName(null); // always null for private
        room = chatRoomRepository.save(room);

        // Both are ADMINs
        memberRepository.save(createMember(room, currentUser, ChatRoomRole.ADMIN));
        memberRepository.save(createMember(room, targetUser, ChatRoomRole.ADMIN));

        log.info(
                "NEW PRIVATE chat CREATED | roomId={} | users=[{}, {}]",
                room.getId(),
                currentUser.getUsername(),
                targetUser.getUsername()
        );

        return mapToResponse(room);
    }

    private ChatRoomMember createMember(
            ChatRoom room,
            ChatUser user,
            ChatRoomRole role
    ) {
        ChatRoomMember member = new ChatRoomMember();
        member.setChatRoom(room);
        member.setUser(user);
        member.setRole(role);
        member.setJoinedAt(Instant.now());
        return member;
    }

    private ChatRoomResponse mapToResponse(ChatRoom room) {
        return new ChatRoomResponse(
                room.getId(),
                room.getType().name(),
                room.getName()
        );
    }
}
