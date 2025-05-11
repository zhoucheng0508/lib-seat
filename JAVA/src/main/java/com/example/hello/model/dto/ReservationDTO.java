package com.example.hello.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

/**
 * 座位预约数据传输对象
 * 用于前后端数据交互
 */
@Data
public class ReservationDTO {
    
    /**
     * 预约ID
     */
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 座位ID
     */
    private String seatId;
    
    /**
     * 座位号
     */
    private String seatNumber;
    
    /**
     * 自习室ID
     */
    private String studyRoomId;
    
    /**
     * 自习室名称
     */
    private String studyRoomName;
    
    /**
     * 预约日期
     */
    private LocalDate date;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 预约状态
     * 可选值：CONFIRMED(已确认), CANCELLED(已取消), COMPLETED(已完成)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 备注信息
     */
    private String remarks;
} 