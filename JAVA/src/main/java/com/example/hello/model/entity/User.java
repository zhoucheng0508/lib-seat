package com.example.hello.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column
    private Integer noShowCount;  // 未签到次数
    
    @Column
    private LocalDateTime blacklistStartTime;  // 进入黑名单的时间
    
    @Column
    private Boolean isBlacklisted;  // 是否在黑名单中
    
    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }
    
    private Long createdAt;
} 