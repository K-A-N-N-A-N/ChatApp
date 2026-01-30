package com.example.ChatApp.service;

import com.example.ChatApp.dto.PresenceUpdate;
import com.example.ChatApp.entity.ChatRoomMember;
import com.example.ChatApp.entity.PresenceStatus;
import com.example.ChatApp.entity.UserPresence;
import com.example.ChatApp.repository.ChatRoomMemberRepository;
import com.example.ChatApp.repository.UserPresenceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPresenceService {

    private final UserPresenceRepository userPresenceRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleConnect(String userId) {
        Instant now = Instant.now();
        UserPresence presence = userPresenceRepository.findById(userId)
                .orElseGet(() -> {
                    UserPresence p = new UserPresence();
                    p.setUserId(userId);
                    return p;
                });

        presence.setStatus(PresenceStatus.ONLINE);
        presence.setLastSeen(now);
        userPresenceRepository.save(presence);

        broadcastPresence(userId, PresenceStatus.ONLINE, now);
    }

    public void handleDisconnect(String userId) {
        Instant now = Instant.now();
        UserPresence presence = userPresenceRepository.findById(userId)
                .orElseGet(() -> {
                    UserPresence p = new UserPresence();
                    p.setUserId(userId);
                    return p;
                });

        presence.setStatus(PresenceStatus.OFFLINE);
        presence.setLastSeen(now);
        userPresenceRepository.save(presence);

        broadcastPresence(userId, PresenceStatus.OFFLINE, now);
    }

    public List<PresenceUpdate> getPresenceForChatRoom(String chatRoomId) {
        List<ChatRoomMember> members = memberRepository.findByChatRoomId(chatRoomId);
        List<String> userIds = members.stream()
                .map(member -> member.getUser().getId())
                .toList();

        if (userIds.isEmpty()) {
            return List.of();
        }

        Map<String, UserPresence> presenceMap = userPresenceRepository.findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(UserPresence::getUserId, p -> p));

        Instant now = Instant.now();
        List<PresenceUpdate> result = new ArrayList<>();

        for (String userId : userIds) {
            UserPresence presence = presenceMap.get(userId);
            if (presence != null) {
                result.add(new PresenceUpdate(
                        userId,
                        presence.getStatus().name(),
                        presence.getLastSeen()
                ));
            } else {
                result.add(new PresenceUpdate(
                        userId,
                        PresenceStatus.OFFLINE.name(),
                        now
                ));
            }
        }

        return result;
    }

    private void broadcastPresence(String userId, PresenceStatus status, Instant at) {
        List<ChatRoomMember> memberships = memberRepository.findByUserId(userId);

        PresenceUpdate payload = new PresenceUpdate(
                userId,
                status.name(),
                at
        );

        memberships.stream()
                .map(m -> m.getChatRoom().getId())
                .distinct()
                .forEach(roomId -> messagingTemplate.convertAndSend(
                        "/topic/chatroom/" + roomId + "/presence",
                        payload
                ));
    }
}

