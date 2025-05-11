package com.example.hello.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.model.entity.Seat;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.service.SeatService;
import com.example.hello.service.SeatStatusCacheService;

/**
 * 座位控制器
 * 处理与座位相关的HTTP请求
 */
@RestController
@RequestMapping("/api/seats")
public class SeatController {

    /**
     * 座位服务
     */
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private SeatStatusCacheService seatStatusCacheService;
    
    /**
     * 获取自习室的所有座位
     * 
     * @param studyRoomId 自习室ID
     * @return 座位列表
     */
    @GetMapping("/study-room/{studyRoomId}")
    public ResponseEntity<?> getSeatsByStudyRoom(@PathVariable String studyRoomId) {
        return seatService.getSeatsByStudyRoom(studyRoomId);
    }
    
    /**
     * 创建新座位
     * 
     * @param seat 座位实体对象
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<?> createSeat(@RequestBody Seat seat) {
        return seatService.createSeat(seat);
    }
    
    /**
     * 批量创建座位
     * 
     * @param studyRoomId 自习室ID
     * @param request 包含座位数量等信息的请求参数
     * @return 创建结果
     */
    @PostMapping("/batch/{studyRoomId}")
    public ResponseEntity<?> createSeatsInBatch(
            @PathVariable String studyRoomId, 
            @RequestBody Map<String, Object> request) {
        try {
            ResponseEntity<?> response = seatService.createSeatsInBatch(studyRoomId, request);
            
            // 如果创建成功，清除该自习室的座位缓存
            if (response.getStatusCode().is2xxSuccessful()) {
                seatStatusCacheService.invalidateStudyRoomSeats(studyRoomId);
            }
            
            return response;
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "批量创建座位失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取座位详情
     * 
     * @param id 座位ID
     * @return 座位信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSeat(@PathVariable String id) {
        return seatService.getSeat(id);
    }
    
    /**
     * 更新座位状态
     * 
     * @param id 座位ID
     * @param status 包含状态信息的Map
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateSeatStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusRequest) {
        try {
            ResponseEntity<?> response = seatService.updateSeatStatus(id, statusRequest);
            
            // 如果更新成功，清除该座位的状态缓存
            if (response.getStatusCode().is2xxSuccessful()) {
                seatStatusCacheService.invalidateSeatStatus(id);
            }
            
            return response;
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "更新座位状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除座位
     * 
     * @param id 座位ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeat(@PathVariable String id) {
        try {
            // 先获取座位信息，以便后续清除缓存
            Seat seat = seatRepository.findById(id).orElse(null);
            
            ResponseEntity<?> response = seatService.deleteSeat(id);
            
            // 如果删除成功，清除相关缓存
            if (response.getStatusCode().is2xxSuccessful() && seat != null) {
                seatStatusCacheService.invalidateSeatStatus(id);
                seatStatusCacheService.invalidateStudyRoomSeats(seat.getStudyRoomId());
            }
            
            return response;
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "删除座位失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除自习室的所有座位
     * 
     * @param studyRoomId 自习室ID
     * @return 删除结果
     */
    @DeleteMapping("/study-room/{studyRoomId}")
    public ResponseEntity<?> deleteAllSeatsByStudyRoom(@PathVariable String studyRoomId) {
        return seatService.deleteAllSeatsByStudyRoom(studyRoomId);
    }
    
    /**
     * 获取座位当前状态（考虑物理状态和当前时间段的预约状态）
     */
    @GetMapping("/{id}/real-time-status")
    public ResponseEntity<?> getSeatRealTimeStatus(
            @PathVariable String id,
            @RequestParam(required = false) String dateStr) {
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        return seatService.getSeatRealTimeStatus(id, date);
    }
    
    /**
     * 获取座位在指定时间段的状态
     */
    @GetMapping("/{id}/status-for-time-slot")
    public ResponseEntity<?> getSeatStatusForTimeSlot(
            @PathVariable String id,
            @RequestParam(required = false) String dateStr,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        return seatService.getSeatStatusForTimeSlot(id, date, start, end);
    }
} 