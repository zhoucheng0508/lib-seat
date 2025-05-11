package com.example.hello.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 自习室座位实体类
 * 包含座位的基本信息，以及与自习室的关联关系
 */
@Data
@Entity
@Table(name = "seats")
public class Seat {
    
    /**
     * 座位ID，主键
     * 系统自动生成，格式为时间戳+UUID
     */
    @Id
    @Column(length = 32)
    private String id;
    
    /**
     * 座位号
     * 如"A1", "B2"等，用于标识自习室内的具体座位
     */
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;
    
    /**
     * 关联的自习室ID
     * 表示该座位属于哪个自习室
     */
    @Column(name = "study_room_id", nullable = false, length = 32)
    private String studyRoomId;
    
    /**
     * 座位状态
     * 可选值：AVAILABLE(可预约), UNAVAILABLE(不可预约), RESERVED(已预约)
     */
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // 默认状态：可预约
    
    /**
     * 实体创建时间
     */
    private Long createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            // 生成UUID作为ID，移除连字符
            id = java.util.UUID.randomUUID().toString().replace("-", "");
        }
        createdAt = System.currentTimeMillis();
    }
} 