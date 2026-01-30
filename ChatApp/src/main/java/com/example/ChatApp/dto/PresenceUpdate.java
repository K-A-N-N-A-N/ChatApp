package com.example.ChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class PresenceUpdate {

    private String userId;
    private String status;
    private Instant lastSeen;
}

