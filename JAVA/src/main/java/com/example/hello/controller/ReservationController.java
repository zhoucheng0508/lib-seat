package com.example.hello.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.exception.CheckInTimeException;
import com.example.hello.exception.ResourceNotFoundException;
import com.example.hello.exception.UnauthorizedException;
import com.example.hello.exception.UserBlacklistedException;
import com.example.hello.model.ReservationStatus;
import com.example.hello.model.entity.Reservation;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.service.ReservationService;
import com.example.hello.service.UserService;

/**
 * 预约控制器
 * 提供预约相关的HTTP API
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建预约
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }
    
    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable String id) {
        return reservationService.getReservation(id);
    }
    
    /**
     * 获取用户的预约列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserReservations(@PathVariable String userId) {
        return reservationService.getUserReservations(userId);
    }
    
    /**
     * 获取用户特定状态的预约
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getUserReservationsByStatus(
            @PathVariable String userId, 
            @PathVariable String status) {
        return reservationService.getUserReservationsByStatus(userId, status);
    }
    
    /**
     * 获取座位的预约列表
     */
    @GetMapping("/seat/{seatId}")
    public ResponseEntity<?> getSeatReservations(@PathVariable String seatId) {
        return reservationService.getSeatReservations(seatId);
    }
    
    /**
     * 获取自习室的预约列表
     */
    @GetMapping("/study-room/{studyRoomId}")
    public ResponseEntity<?> getStudyRoomReservations(@PathVariable String studyRoomId) {
        return reservationService.getStudyRoomReservations(studyRoomId);
    }
    
    /**
     * 获取特定日期的预约列表
     */
    @GetMapping("/date/{dateStr}")
    public ResponseEntity<?> getReservationsByDate(@PathVariable String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return reservationService.getReservationsByDate(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(java.util.Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }
    
    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable String id) {
        return reservationService.cancelReservation(id);
    }
    
    /**
     * 完成预约
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeReservation(@PathVariable String id) {
        return reservationService.completeReservation(id);
    }
    
    /**
     * 检查座位在指定时间段是否可用
     */
    @GetMapping("/check-availability")
    public ResponseEntity<?> checkSeatAvailability(
            @RequestParam String seatId,
            @RequestParam String dateStr,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return reservationService.checkSeatAvailability(seatId, date, startTime, endTime);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(java.util.Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }
    
    /**
     * 获取自习室在特定日期的可用时间段
     */
    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableTimeSlots(
            @RequestParam String studyRoomId,
            @RequestParam String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return reservationService.getAvailableTimeSlots(studyRoomId, date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(java.util.Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }
    
    /**
     * 获取自习室在指定时间段的状态
     * 如果未指定时间，则使用当前时间
     */
    @GetMapping("/study-room/{studyRoomId}/status")
    public ResponseEntity<?> getStudyRoomStatus(
            @PathVariable String studyRoomId,
            @RequestParam(required = false) String dateStr,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDate date = dateStr != null ? 
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE) : 
                LocalDate.now();
            return reservationService.getStudyRoomStatus(studyRoomId, date, startTime, endTime);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }
    
    /**
     * 获取自习室在指定时间段的所有座位状态
     * 如果未指定时间，则使用当前时间
     */
    @GetMapping("/study-room/{studyRoomId}/seats-status")
    public ResponseEntity<?> getStudyRoomSeatsStatus(
            @PathVariable String studyRoomId,
            @RequestParam(required = false) String dateStr,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDate date = dateStr != null ? 
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE) : 
                LocalDate.now();
            return reservationService.getStudyRoomSeatsStatus(studyRoomId, date, startTime, endTime);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }

    /**
     * 获取所有自习室在指定时间段的状态
     * 如果未指定时间，则使用当前时间
     */
    @GetMapping("/study-rooms/status")
    public ResponseEntity<?> getStudyRoomsStatus(
            @RequestParam(required = false) String dateStr,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDate date = dateStr != null ? 
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE) : 
                LocalDate.now();
            return reservationService.getStudyRoomsStatus(date, startTime, endTime);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }

    /**
     * 获取自习室详情和座位信息
     * 如果未指定时间，则使用当前时间
     */
    @GetMapping("/study-rooms/{studyRoomId}/detail")
    public ResponseEntity<?> getStudyRoomDetail(
            @PathVariable String studyRoomId,
            @RequestParam(required = false) String dateStr,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDate date = dateStr != null ? 
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE) : 
                LocalDate.now();
            return reservationService.getStudyRoomDetail(studyRoomId, date, startTime, endTime);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkIn(@PathVariable String id, @RequestHeader("X-User-ID") String userId) {
        try {
            // 1. 验证预约是否存在且属于该用户
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
            
            if (!reservation.getUserId().equals(userId)) {
                throw new UnauthorizedException("无权操作此预约");
            }
            
            // 2. 检查用户是否在黑名单中
            Map<String, Object> blacklistStatus = userService.getUserBlacklistStatus(userId);
            if (Boolean.TRUE.equals(blacklistStatus.get("isBlacklisted"))) {
                throw new UserBlacklistedException((Long) blacklistStatus.get("remainingTime"));
            }
            
            // 3. 检查当前时间是否在预约时间段内
            // 使用北京时间（UTC+8）
            ZoneId beijingZone = ZoneId.of("Asia/Shanghai");
            LocalDateTime now = LocalDateTime.now(beijingZone);
            LocalDateTime startTime = LocalDateTime.of(reservation.getDate(), reservation.getStartTime())
                    .atZone(beijingZone)
                    .toLocalDateTime();
            LocalDateTime endTime = LocalDateTime.of(reservation.getDate(), reservation.getEndTime())
                    .atZone(beijingZone)
                    .toLocalDateTime();
            
            // 计算允许的最早签到时间（提前15分钟）
            LocalDateTime earliestCheckInTime = startTime.minusMinutes(15);
            
            // 添加调试日志
            System.out.println("当前时间(北京时间): " + now);
            System.out.println("最早签到时间: " + earliestCheckInTime);
            System.out.println("预约开始时间: " + startTime);
            System.out.println("预约结束时间: " + endTime);
            
            if (now.isBefore(earliestCheckInTime)) {
                throw new CheckInTimeException("签到时间过早，请在预约开始时间前15分钟内签到（最早签到时间：" + 
                    earliestCheckInTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "）");
            }
            
            if (now.isAfter(endTime)) {
                throw new CheckInTimeException("签到时间已过，预约结束时间为 " + 
                    reservation.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            
            // 4. 更新预约状态为已签到
            reservation.setStatus(ReservationStatus.CHECKED_IN.name());
            reservationRepository.save(reservation);
            
            return ResponseEntity.ok(Map.of("message", "签到成功"));
        } catch (CheckInTimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (UserBlacklistedException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "您已被加入黑名单，剩余时间：" + e.getRemainingTime() + "毫秒"));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound()
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "签到失败：" + e.getMessage()));
        }
    }
} 