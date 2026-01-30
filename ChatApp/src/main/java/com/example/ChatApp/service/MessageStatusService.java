package com.example.ChatApp.service;

import com.example.ChatApp.entity.ChatRoomMember;
import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.entity.Message;
import com.example.ChatApp.entity.MessageStatus;
import com.example.ChatApp.entity.MessageStatusType;
import com.example.ChatApp.repository.ChatRoomMemberRepository;
import com.example.ChatApp.repository.MessageRepository;
import com.example.ChatApp.repository.MessageStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageStatusService {

    private final MessageStatusRepository messageStatusRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final MessageRepository messageRepository;

    public void createDeliveredStatusesForMessage(Message message) {
        String chatRoomId = message.getChatRoom().getId();
        String senderId = message.getSender().getId();

        List<ChatRoomMember> members = memberRepository.findByChatRoomId(chatRoomId);
        Instant now = Instant.now();

        List<MessageStatus> statuses = members.stream()
                .filter(member -> !member.getUser().getId().equals(senderId))
                .map(member -> {
                    MessageStatus status = new MessageStatus();
                    status.setMessage(message);
                    status.setUser(member.getUser());
                    status.setStatus(MessageStatusType.DELIVERED);
                    status.setTimestamp(now);
                    return status;
                })
                .toList();

        if (!statuses.isEmpty()) {
            messageStatusRepository.saveAll(statuses);
        }
    }

    public MessageStatus markAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Ensure the user belongs to the same chat room
        String chatRoomId = message.getChatRoom().getId();
        boolean isMember = memberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
        if (!isMember) {
            throw new RuntimeException("User is not a member of this chat room");
        }

        MessageStatus status = messageStatusRepository
                .findByMessageIdAndUserId(messageId, userId)
                .orElseGet(() -> {
                    MessageStatus s = new MessageStatus();
                    s.setMessage(message);
                    ChatUser userRef = new ChatUser();
                    userRef.setId(userId);
                    s.setUser(userRef);
                    return s;
                });

        status.setStatus(MessageStatusType.READ);
        status.setTimestamp(Instant.now());

        return messageStatusRepository.save(status);
    }

    public long countByStatus(String messageId, MessageStatusType status) {
        return messageStatusRepository.countByMessageIdAndStatus(messageId, status);
    }
}

