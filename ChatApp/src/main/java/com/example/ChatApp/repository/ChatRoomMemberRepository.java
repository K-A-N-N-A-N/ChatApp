package com.example.ChatApp.repository;

import com.example.ChatApp.entity.ChatRoomMember;
import com.example.ChatApp.entity.ChatRoomRole;
import com.example.ChatApp.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, String> {

    @Query("""
        SELECT crm.chatRoom.id
        FROM ChatRoomMember crm
        WHERE crm.user.id IN (:userA, :userB)
          AND crm.chatRoom.type = :type
        GROUP BY crm.chatRoom.id
        HAVING COUNT(DISTINCT crm.user.id) = 2
    """)
    List<String> findExistingPrivateRoom(
            @Param("userA") String userA,
            @Param("userB") String userB,
            @Param("type") ChatRoomType type
    );

    boolean existsByChatRoomIdAndUserId(String chatRoomId, String userId);

    List<ChatRoomMember> findByChatRoomId(String chatRoomId);

    long countByChatRoomIdAndRole(String chatRoomId, ChatRoomRole role);

    Optional<ChatRoomMember> findByChatRoomIdAndUserId(
            String chatRoomId,
            String userId
    );
}


