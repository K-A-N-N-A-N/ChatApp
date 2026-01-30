package com.example.ChatApp.controller;

import com.example.ChatApp.dto.LoginRequest;
import com.example.ChatApp.dto.LoginResponse;
import com.example.ChatApp.entity.ChatUser;
import com.example.ChatApp.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out successfully");
    }

    @GetMapping("/me")
    public Object me(HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        String username = (String) session.getAttribute("USERNAME");

        if (userId == null || username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        return Map.of("userId", userId, "username", username);
    }
}

