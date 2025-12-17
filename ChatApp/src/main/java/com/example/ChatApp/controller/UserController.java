package com.example.ChatApp.controller;

import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ChatUser createUser(@Valid @RequestBody ChatUser user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public ChatUser getUser(@PathVariable String id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}/deactivate")
    public ChatUser deactivateUser(@PathVariable String id) {
        return userService.deactivateUser(id);
    }
}
