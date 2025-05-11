package com.example.hello.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "study_rooms")
public class StudyRoom {
    /**
     * 自习室ID，主键
     */
    @Id
    @Column(length = 32)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String location;
    
    @Column(nullable = false)
    private Integer capacity;
    
    private String description;
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "open_time", nullable = false)
    private String openTime = "08:00";  // 默认开放时间
    
    @Column(name = "close_time")
    private String closeTime;
    
    @Column(name = "max_advance_days")
    private Integer maxAdvanceDays;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "image_url")
    private String imageUrl = "https://example.com/default-study-room.jpg";  // 默认图片URL
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            // 生成UUID作为ID
            id = java.util.UUID.randomUUID().toString().replace("-", "");
        }
        createdAt = System.currentTimeMillis();
    }
} 