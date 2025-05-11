package com.example.hello.model.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String userId;
    private String username;
    private String token;
    private Long createdAt;
} 