package com.example.ChatApp.repository;

import com.example.ChatApp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);

}
