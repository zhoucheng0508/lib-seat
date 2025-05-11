package com.example.hello.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class AdminReservationDTO {
    private String id;
    private String userId;
    private String username;  // 用户名
    private String seatId;
    private String seatNumber;  // 座位号
    private String studyRoomId;
    private String studyRoomName;  // 自习室名称
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Boolean isDeleted;
    private String deletedBy;
    private LocalDate deletedAt;
    private String adjustedBy;
    private LocalDate adjustedAt;
    
    public AdminReservationDTO(
            String id,
            String userId,
            String username,
            String seatId,
            String seatNumber,
            String studyRoomId,
            String studyRoomName,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String status,
            Boolean isDeleted,
            String deletedBy,
            LocalDate deletedAt,
            String adjustedBy,
            LocalDate adjustedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.studyRoomId = studyRoomId;
        this.studyRoomName = studyRoomName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.isDeleted = isDeleted;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        this.adjustedBy = adjustedBy;
        this.adjustedAt = adjustedAt;
    }
} 