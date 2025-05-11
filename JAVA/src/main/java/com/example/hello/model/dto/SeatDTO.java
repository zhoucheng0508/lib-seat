package com.example.hello.model.dto;

import lombok.Data;

/**
 * 自习室座位数据传输对象
 * 用于前后端数据交互
 */
@Data
public class SeatDTO {
    /**
     * 座位ID
     */
    private String id;
    
    /**
     * 座位号
     */
    private String seatNumber;
    
    /**
     * 关联的自习室ID
     */
    private String studyRoomId;
    
    /**
     * 关联的自习室名称
     */
    private String studyRoomName;
    
    /**
     * 座位状态
     * 可选值：AVAILABLE(可预约), UNAVAILABLE(不可预约), RESERVED(已预约)
     */
    private String status;
} 