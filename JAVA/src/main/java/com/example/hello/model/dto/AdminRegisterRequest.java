package com.example.hello.model.dto;

import lombok.Data;

@Data
public class AdminRegisterRequest {
    private String username;
    private String password;
    private String verificationCode;
} 