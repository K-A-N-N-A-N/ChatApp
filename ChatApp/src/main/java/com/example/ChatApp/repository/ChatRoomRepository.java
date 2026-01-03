package com.example.ChatApp.repository;

import com.example.ChatApp.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("""
        SELECT crm.chatRoom
        FROM ChatRoomMember crm
        WHERE crm.user.id = :userId
    """)
    List<ChatRoom> findAllByUserId(@Param("userId") String userId);
}
