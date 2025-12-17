package com.example.ChatApp.service;

import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ChatUser createUser(ChatUser user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        user.setActive(true);
        return userRepository.save(user);
    }

    public ChatUser getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ChatUser deactivateUser(String userId) {
        ChatUser user = getUser(userId);
        user.setActive(false);
        return userRepository.save(user);
    }
}
