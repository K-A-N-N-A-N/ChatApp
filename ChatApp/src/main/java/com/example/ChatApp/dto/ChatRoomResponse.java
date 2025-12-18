package com.example.ChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomResponse {

    private String id;
    private String type;
    private String name;
}
