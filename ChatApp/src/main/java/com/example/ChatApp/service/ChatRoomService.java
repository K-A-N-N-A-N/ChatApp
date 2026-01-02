package com.example.ChatApp.service;

import com.example.ChatApp.dto.ChatRoomListResponse;
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

    public ChatRoomResponse createPrivateChatByUsername(
            String currentUserId,
            String targetUsername
    ) {
        ChatUser targetUser = userRepository.findByUsernameAndActiveTrue(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        return createPrivateChat(currentUserId, targetUser.getId());
    }

    // Create Group Chat
    public ChatRoomResponse createGroupChat(String currentUserId, String groupName) {

        ChatUser creator = userRepository.findByIdAndActiveTrue(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomType.GROUP);
        room.setName(groupName);

        room = chatRoomRepository.save(room);

        // creator is ADMIN
        memberRepository.save(
                createMember(room, creator, ChatRoomRole.ADMIN)
        );

        log.info(
                "NEW GROUP chat CREATED | roomId={} | name={} | admin={}",
                room.getId(), groupName, creator.getUsername()
        );

        return mapToResponse(room);
    }

    public void addMemberToGroup(
            String adminUserId,
            String chatRoomId,
            String targetUsername
    ) {

        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (room.getType() != ChatRoomType.GROUP) {
            throw new RuntimeException("Not a group chat");
        }

        // verify admin
        ChatRoomMember adminMember =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, adminUserId)
                        .orElseThrow(() -> new RuntimeException("Not a member"));

        if (adminMember.getRole() != ChatRoomRole.ADMIN) {
            throw new RuntimeException("Only admins can add members");
        }

        ChatUser targetUser =
                userRepository.findByUsernameAndActiveTrue(targetUsername)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyMember =
                memberRepository.existsByChatRoomIdAndUserId(chatRoomId, targetUser.getId());

        if (alreadyMember) {
            throw new RuntimeException("User already in group");
        }

        memberRepository.save(
                createMember(room, targetUser, ChatRoomRole.MEMBER)
        );

        log.info(
                "USER ADDED to GROUP | roomId={} | user={} | byAdmin={}",
                chatRoomId, targetUsername, adminUserId
        );
    }

    public void removeMemberFromGroup(
            String adminUserId,
            String chatRoomId,
            String targetUsername
    ) {

        // Verify admin user
        ChatRoomMember admin =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, adminUserId)
                        .orElseThrow(() -> new RuntimeException("Not a member"));

        if (admin.getRole() != ChatRoomRole.ADMIN) {
            throw new RuntimeException("Only admins can remove members");
        }

        // Find target user
        ChatUser targetUser =
                userRepository.findByUsernameAndActiveTrue(targetUsername)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoomMember target =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, targetUser.getId())
                        .orElseThrow(() -> new RuntimeException("User not in group"));

        // 🔐 SAFETY CHECK: if target is ADMIN, ensure not last admin
        if (target.getRole() == ChatRoomRole.ADMIN) {
            long adminCount =
                    memberRepository.countByChatRoomIdAndRole(
                            chatRoomId,
                            ChatRoomRole.ADMIN
                    );

            if (adminCount <= 1) {
                throw new RuntimeException("Cannot remove the last admin from the group");
            }
        }

        memberRepository.delete(target);

        log.info(
                "USER REMOVED from GROUP | roomId={} | user={} | removedBy={}",
                chatRoomId,
                targetUsername,
                adminUserId
        );
    }


    public List<ChatRoomListResponse> listMyChatRooms(String userId) {

        List<ChatRoom> rooms = chatRoomRepository.findAllByUserId(userId);

        return rooms.stream().map(room -> {

            List<String> members =
                    memberRepository.findByChatRoomId(room.getId())
                            .stream()
                            .map(m -> m.getUser().getUsername())
                            .toList();

            return new ChatRoomListResponse(
                    room.getId(),
                    room.getType().name(),
                    room.getName(),
                    members
            );

        }).toList();
    }

    public void promoteToAdmin(
            String adminUserId,
            String chatRoomId,
            String targetUsername
    ) {

        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (room.getType() != ChatRoomType.GROUP) {
            throw new RuntimeException("Only group chats support roles");
        }

        ChatRoomMember admin =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, adminUserId)
                        .orElseThrow(() -> new RuntimeException("Not a member"));

        if (admin.getRole() != ChatRoomRole.ADMIN) {
            throw new RuntimeException("Only admins can promote members");
        }

        ChatUser targetUser =
                userRepository.findByUsernameAndActiveTrue(targetUsername)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoomMember target =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, targetUser.getId())
                        .orElseThrow(() -> new RuntimeException("User not in group"));

        if (target.getRole() == ChatRoomRole.ADMIN) {
            throw new RuntimeException("User is already admin");
        }

        target.setRole(ChatRoomRole.ADMIN);
        memberRepository.save(target);

        log.info(
                "PROMOTED TO ADMIN | roomId={} | user={} | byAdmin={}",
                chatRoomId, targetUsername, adminUserId
        );
    }

    public void demoteToMember(
            String adminUserId,
            String chatRoomId,
            String targetUsername
    ) {

        ChatRoomMember admin =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, adminUserId)
                        .orElseThrow(() -> new RuntimeException("Not a member"));

        if (admin.getRole() != ChatRoomRole.ADMIN) {
            throw new RuntimeException("Only admins can demote admins");
        }

        ChatUser targetUser =
                userRepository.findByUsernameAndActiveTrue(targetUsername)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoomMember target =
                memberRepository.findByChatRoomIdAndUserId(chatRoomId, targetUser.getId())
                        .orElseThrow(() -> new RuntimeException("User not in group"));

        if (target.getRole() != ChatRoomRole.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        long adminCount =
                memberRepository.countByChatRoomIdAndRole(chatRoomId, ChatRoomRole.ADMIN);

        if (adminCount <= 1) {
            throw new RuntimeException("Cannot demote the last admin");
        }

        target.setRole(ChatRoomRole.MEMBER);
        memberRepository.save(target);

        log.info(
                "DEMOTED TO MEMBER | roomId={} | user={} | byAdmin={}",
                chatRoomId, targetUsername, adminUserId
        );
    }

}
