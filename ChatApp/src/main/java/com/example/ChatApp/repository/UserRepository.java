package com.example.ChatApp.repository;

import com.example.ChatApp.entity.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ChatUser, String> {

    Optional<ChatUser> findByUsername(String username);
    Optional<ChatUser> findByIdAndActiveTrue(String id);

    boolean existsByUsername(String username);
}
