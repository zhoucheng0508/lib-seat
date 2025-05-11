package com.example.hello.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 座位预约实体类
 * 记录用户对座位的预约信息
 */
@Data
@Entity
@Table(name = "reservations")
public class Reservation {
    
    /**
     * 预约ID，主键
     * 系统自动生成，格式为时间戳+UUID
     */
    @Id
    @Column(length = 32)
    private String id;
    
    /**
     * 用户ID
     * 进行预约的用户
     */
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    /**
     * 座位ID
     * 被预约的座位
     */
    @Column(name = "seat_id", nullable = false)
    private String seatId;
    
    /**
     * 自习室ID
     * 座位所属的自习室
     */
    @Column(name = "study_room_id", nullable = false)
    private String studyRoomId;
    
    /**
     * 预约日期
     * 预约的日期，格式：yyyy-MM-dd
     */
    @Column(nullable = false)
    private LocalDate date;
    
    /**
     * 开始时间
     * 预约的开始时间，格式：HH:mm
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    /**
     * 结束时间
     * 预约的结束时间，格式：HH:mm
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    /**
     * 预约状态
     * 可选值：CONFIRMED(已确认), CANCELLED(已取消), COMPLETED(已完成)
     */
    @Column(nullable = false)
    private String status = "CONFIRMED";
    
    /**
     * 创建时间
     * 预约创建的时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     * 预约最后更新的时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 软删除相关字段
     */
    private Boolean isDeleted;
    private String deletedBy;
    private LocalDate deletedAt;
    
    /**
     * 状态调整相关字段
     */
    private String adjustedBy;
    private LocalDate adjustedAt;
    
    /**
     * 实体创建前的预处理
     * 自动生成ID和设置时间
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 19);
            this.id = timestamp + uuid;
        }
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
} 