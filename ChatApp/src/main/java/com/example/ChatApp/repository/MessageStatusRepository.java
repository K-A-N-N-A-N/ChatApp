package com.example.ChatApp.repository;

import com.example.ChatApp.entity.MessageStatus;
import com.example.ChatApp.entity.MessageStatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, String> {

    long countByMessageIdAndStatus(String messageId, MessageStatusType status);

    Optional<MessageStatus> findByMessageIdAndUserId(String messageId, String userId);

    List<MessageStatus> findByMessageIdInAndStatus(List<String> messageIds, MessageStatusType status);
}

