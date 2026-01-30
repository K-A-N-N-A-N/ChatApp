package com.example.ChatApp.repository;

import com.example.ChatApp.entity.UserPresence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPresenceRepository extends JpaRepository<UserPresence, String> {

    List<UserPresence> findByUserIdIn(List<String> userIds);
}

