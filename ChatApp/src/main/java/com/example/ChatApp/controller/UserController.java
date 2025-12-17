package com.example.ChatApp.controller;

import com.example.ChatApp.dto.LoginRequest;
import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ChatUser createUser(@RequestBody ChatUser user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public ChatUser login(@RequestBody LoginRequest request) {
        return userService.login(request.username(), request.password());
    }

    @GetMapping("/{id}")
    public ChatUser getUser(@PathVariable String id) {
        return userService.getUser(id);
    }

    @GetMapping
    public List<ChatUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/deactivate")
    public ChatUser deactivateUser(@PathVariable String id) {
        return userService.deactivateUser(id);
    }
}
