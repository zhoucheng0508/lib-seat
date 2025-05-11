package com.example.hello.model.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String username;
    private Long createdAt;
    // 不包含密码字段
} 