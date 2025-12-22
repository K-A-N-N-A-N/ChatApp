package com.example.ChatApp.controller;

import com.example.ChatApp.dto.LoginRequest;
import com.example.ChatApp.dto.LoginResponse;
import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request,
            HttpSession session
    ) {
        ChatUser user = userService.login(
                request.username(),
                request.password()
        );

        // Store user context in session
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USERNAME", user.getUsername());

        return new LoginResponse(
                user.getId(),
                user.getUsername()
        );
    }

    @GetMapping("/me")
    public Object me(HttpSession session) {
        return Map.of(
                "userId", session.getAttribute("USER_ID"),
                "username", session.getAttribute("USERNAME")
        );
    }
}

