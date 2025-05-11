package com.example.hello.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.hello.dto.AdminReservationDTO;

public interface AdminReservationService {
    /**
     * 分页查询预约记录
     */
    Page<AdminReservationDTO> getReservations(
        String userId,
        String startDate,
        String endDate,
        String status,
        String seatId,
        String studyRoomId,
        Pageable pageable);
        
    /**
     * 删除预约记录
     */
    void deleteReservation(String id, String adminId);
    
    /**
     * 调整预约状态
     */
    void adjustReservationStatus(String id, String adminId);
} 