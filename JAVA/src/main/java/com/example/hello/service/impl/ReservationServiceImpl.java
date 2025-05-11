package com.example.hello.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.model.dto.ReservationDTO;
import com.example.hello.model.entity.Reservation;
import com.example.hello.model.entity.Seat;
import com.example.hello.model.entity.StudyRoom;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.repository.StudyRoomRepository;
import com.example.hello.repository.UserRepository;
import com.example.hello.service.ReservationService;
import com.example.hello.service.SeatStatusCacheService;

/**
 * 预约服务实现类
 * 提供预约相关的业务逻辑处理
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    /**
     * 预约数据访问对象
     */
    @Autowired
    private ReservationRepository reservationRepository;
    
    /**
     * 座位数据访问对象
     */
    @Autowired
    private SeatRepository seatRepository;
    
    /**
     * 自习室数据访问对象
     */
    @Autowired
    private StudyRoomRepository studyRoomRepository;
    
    /**
     * 用户数据访问对象
     */
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeatStatusCacheService seatStatusCacheService;
    
    /**
     * 将预约实体对象转换为DTO对象
     * 添加用户名、座位号、自习室名称等信息
     * 
     * @param reservation 预约实体对象
     * @return 预约DTO对象
     */
    @Override
    public ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUserId());
        dto.setSeatId(reservation.getSeatId());
        dto.setStudyRoomId(reservation.getStudyRoomId());
        dto.setDate(reservation.getDate());
        dto.setStartTime(reservation.getStartTime());
        dto.setEndTime(reservation.getEndTime());
        dto.setStatus(reservation.getStatus());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setRemarks(reservation.getRemarks());
        
        // 添加用户名
        userRepository.findById(reservation.getUserId())
            .ifPresent(user -> dto.setUsername(user.getUsername()));
        
        // 添加座位号
        seatRepository.findById(reservation.getSeatId())
            .ifPresent(seat -> dto.setSeatNumber(seat.getSeatNumber()));
        
        // 添加自习室名称
        studyRoomRepository.findById(reservation.getStudyRoomId())
            .ifPresent(room -> dto.setStudyRoomName(room.getName()));
        
        return dto;
    }
    
    /**
     * 创建新预约
     */
    @Override
    @Transactional
    public ResponseEntity<?> createReservation(Reservation reservation) {
        // 验证必要字段是否为空
        if (reservation.getUserId() == null || reservation.getUserId().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "用户ID不能为空"));
        }
        
        if (reservation.getSeatId() == null || reservation.getSeatId().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "座位ID不能为空"));
        }
        
        if (reservation.getStudyRoomId() == null || reservation.getStudyRoomId().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "自习室ID不能为空"));
        }
        
        // 验证用户是否存在
        if (!userRepository.existsById(reservation.getUserId())) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "用户不存在"));
        }
        
        // 检查用户是否在黑名单中
        var user = userRepository.findById(reservation.getUserId()).orElse(null);
        if (user != null && Boolean.TRUE.equals(user.getIsBlacklisted())) {
            // 计算剩余黑名单时间
            long remainingTime = 0;
            if (user.getBlacklistStartTime() != null) {
                LocalDateTime endTime = user.getBlacklistStartTime().plusDays(2);
                remainingTime = java.time.temporal.ChronoUnit.MILLIS.between(
                    LocalDateTime.now(), endTime
                );
                remainingTime = Math.max(0, remainingTime);
            }
            
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "您已被加入黑名单，暂时无法预约",
                    "remainingTime", remainingTime
                ));
        }
        
        // 验证座位是否存在
        Seat seat = seatRepository.findById(reservation.getSeatId()).orElse(null);
        if (seat == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "座位不存在"));
        }
        
        // 验证自习室是否存在
        StudyRoom studyRoom = studyRoomRepository.findById(reservation.getStudyRoomId()).orElse(null);
        if (studyRoom == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "自习室不存在"));
        }
        
        // 验证座位的物理状态是否可用
        if (!"AVAILABLE".equals(seat.getStatus())) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "该座位当前不可预约，物理状态为：" + seat.getStatus()));
        }
        
        // 验证预约时间是否在自习室开放时间内
        LocalTime roomOpenTime = LocalTime.parse(studyRoom.getOpenTime());
        LocalTime roomCloseTime = LocalTime.parse(studyRoom.getCloseTime());
        
        if (reservation.getStartTime().isBefore(roomOpenTime) || 
            reservation.getEndTime().isAfter(roomCloseTime)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", 
                    "预约时间必须在自习室开放时间内（" + 
                    studyRoom.getOpenTime() + " - " + 
                    studyRoom.getCloseTime() + "）"));
        }
        
        // 验证预约日期是否在允许的提前预约天数范围内（固定7天）
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(7); // 固定为7天
        if (reservation.getDate().isBefore(today) || 
            reservation.getDate().isAfter(maxDate)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", 
                    "预约日期必须在当前日期到未来7天内"));
        }
        
        // 检查时间段是否已被预约（动态状态检查）
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
            reservation.getSeatId(), 
            reservation.getDate(), 
            reservation.getStartTime(), 
            reservation.getEndTime());
            
        if (!overlappingReservations.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "该时间段已被预约",
                    "conflict", overlappingReservations.stream()
                        .map(this::convertToDTO)
                        .findFirst()
                        .orElse(null)
                ));
        }
        
        // 检查用户是否在同一时间段预约了其他座位
        List<Reservation> userOverlappingReservations = reservationRepository.findUserOverlappingReservations(
            reservation.getUserId(), 
            reservation.getDate(),
            reservation.getStartTime(),
            reservation.getEndTime());
            
        if (!userOverlappingReservations.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "您在该时间段已预约了其他座位，不能同时预约多个座位",
                    "conflict", userOverlappingReservations.stream()
                        .map(this::convertToDTO)
                        .findFirst()
                        .orElse(null)
                ));
        }
        
        // 检查用户当天预约次数
        long userReservationsToday = reservationRepository.countByUserIdAndDateAndStatusNot(
            reservation.getUserId(), reservation.getDate(), "CANCELLED");
            
        if (userReservationsToday >= 3) {  // 假设每天最多允许3次预约
            return ResponseEntity.badRequest()
                .body(Map.of("message", "您今天的预约次数已达上限"));
        }
        
        // 设置预约状态为已确认
        reservation.setStatus("CONFIRMED");
        
        // 保存预约
        Reservation savedReservation = reservationRepository.save(reservation);
        
        return ResponseEntity.ok(convertToDTO(savedReservation));
    }
    
    /**
     * 获取预约详情
     */
    @Override
    public ResponseEntity<?> getReservation(String id) {
        try {
            return reservationRepository.findById(id)
                .map(reservation -> ResponseEntity.ok(convertToDTO(reservation)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取预约信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户的预约列表
     */
    @Override
    public ResponseEntity<?> getUserReservations(String userId) {
        try {
            List<Reservation> reservations = reservationRepository.findByUserId(userId);
            List<ReservationDTO> dtos = reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取用户预约列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取特定状态的用户预约
     */
    @Override
    public ResponseEntity<?> getUserReservationsByStatus(String userId, String status) {
        try {
            List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, status);
            List<ReservationDTO> dtos = reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取用户特定状态预约失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取座位的预约列表
     */
    @Override
    public ResponseEntity<?> getSeatReservations(String seatId) {
        try {
            // 验证座位是否存在
            if (!seatRepository.existsById(seatId)) {
                return ResponseEntity.notFound().build();
            }
            
            List<Reservation> reservations = reservationRepository.findBySeatId(seatId);
            List<ReservationDTO> dtos = reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取座位预约列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取自习室的预约列表
     */
    @Override
    public ResponseEntity<?> getStudyRoomReservations(String studyRoomId) {
        try {
            // 验证自习室是否存在
            if (!studyRoomRepository.existsById(studyRoomId)) {
                return ResponseEntity.notFound().build();
            }
            
            List<Reservation> reservations = reservationRepository.findByStudyRoomId(studyRoomId);
            List<ReservationDTO> dtos = reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室预约列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取特定日期的预约列表
     */
    @Override
    public ResponseEntity<?> getReservationsByDate(LocalDate date) {
        try {
            List<Reservation> reservations = reservationRepository.findByDate(date);
            List<ReservationDTO> dtos = reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取日期预约列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 取消预约
     */
    @Override
    @Transactional
    public ResponseEntity<?> cancelReservation(String id) {
        try {
            return reservationRepository.findById(id)
                .map(reservation -> {
                    // 只有"已确认"状态的预约才能取消
                    if (!"CONFIRMED".equals(reservation.getStatus())) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("message", "只有已确认的预约才能取消"));
                    }
                    
                    // 设置状态为"已取消"
                    reservation.setStatus("CANCELLED");
                    Reservation updatedReservation = reservationRepository.save(reservation);
                    
                    return ResponseEntity.ok(convertToDTO(updatedReservation));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "取消预约失败: " + e.getMessage()));
        }
    }
    
    /**
     * 完成预约
     */
    @Override
    @Transactional
    public ResponseEntity<?> completeReservation(String id) {
        try {
            return reservationRepository.findById(id)
                .map(reservation -> {
                    // 只有"已确认"状态的预约才能标记为完成
                    if (!"CONFIRMED".equals(reservation.getStatus())) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("message", "只有已确认的预约才能标记为完成"));
                    }
                    
                    // 设置状态为"已完成"
                    reservation.setStatus("COMPLETED");
                    Reservation updatedReservation = reservationRepository.save(reservation);
                    
                    return ResponseEntity.ok(convertToDTO(updatedReservation));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "完成预约失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查座位在指定时间段是否可用
     */
    @Override
    public ResponseEntity<?> checkSeatAvailability(String seatId, LocalDate date, String startTimeStr, String endTimeStr) {
        try {
            // 验证座位是否存在
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "座位不存在"));
            }
            
            // 解析时间
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            
            // 验证预约时间是否在自习室开放时间内
            StudyRoom studyRoom = studyRoomRepository.findById(seat.getStudyRoomId()).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "找不到座位对应的自习室"));
            }
            
            LocalTime roomOpenTime = LocalTime.parse(studyRoom.getOpenTime());
            LocalTime roomCloseTime = LocalTime.parse(studyRoom.getCloseTime());
            
            if (startTime.isBefore(roomOpenTime) || endTime.isAfter(roomCloseTime)) {
                return ResponseEntity.ok(Map.of(
                    "available", false,
                    "message", "预约时间必须在自习室开放时间内（" + 
                        studyRoom.getOpenTime() + " - " + 
                        studyRoom.getCloseTime() + "）"
                ));
            }
            
            // 验证预约日期是否在允许的提前预约天数范围内（固定7天）
            LocalDate today = LocalDate.now();
            LocalDate maxDate = today.plusDays(7); // 固定为7天
            if (date.isBefore(today) || date.isAfter(maxDate)) {
                return ResponseEntity.ok(Map.of(
                    "available", false,
                    "message", "预约日期必须在当前日期到未来7天内"
                ));
            }
            
            // 检查座位物理状态是否可用
            if (!"AVAILABLE".equals(seat.getStatus())) {
                return ResponseEntity.ok(Map.of(
                    "available", false,
                    "message", "该座位当前不可预约，状态为：" + seat.getStatus()
                ));
            }
            
            // 检查时间段是否已被预约（动态状态检查）
            List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                seatId, date, startTime, endTime);
                
            if (!overlappingReservations.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "available", false,
                    "message", "该时间段已被预约",
                    "overlappingReservations", overlappingReservations.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList())
                ));
            }
            
            // 所有检查都通过，座位可预约
            return ResponseEntity.ok(Map.of(
                "available", true,
                "message", "座位可预约",
                "seatInfo", Map.of(
                    "id", seat.getId(),
                    "seatNumber", seat.getSeatNumber(),
                    "studyRoomId", seat.getStudyRoomId(),
                    "studyRoomName", studyRoom.getName(),
                    "physicalStatus", seat.getStatus()
                ),
                "timeInfo", Map.of(
                    "date", date.toString(),
                    "startTime", startTimeStr,
                    "endTime", endTimeStr
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "检查座位可用性失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取自习室在特定日期的可用时间段
     */
    @Override
    public ResponseEntity<?> getAvailableTimeSlots(String studyRoomId, LocalDate date) {
        try {
            // 验证自习室是否存在
            StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室不存在"));
            }
            
            // 检查自习室是否可用
            if (!"AVAILABLE".equals(studyRoom.getStatus())) {
                return ResponseEntity.ok(Map.of(
                    "status", studyRoom.getStatus(),
                    "message", "自习室当前不可用，状态为：" + studyRoom.getStatus()
                ));
            }

            // 获取自习室的所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            
            // 如果没有座位，返回空结果
            if (seats.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "studyRoomId", studyRoomId,
                    "date", date.toString(),
                    "openTime", studyRoom.getOpenTime(),
                    "closeTime", studyRoom.getCloseTime(),
                    "availableSeats", 0,
                    "timeSlots", List.of()
                ));
            }
            
            // 获取该日期该自习室的所有预约
            List<Reservation> reservations = reservationRepository.findByStudyRoomIdAndDate(studyRoomId, date);
            
            // 计算每个座位的可用时间段
            Map<String, List<Map<String, String>>> seatAvailability = seats.stream()
                .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                .collect(Collectors.toMap(
                    Seat::getId,
                    seat -> {
                        // 该座位的所有预约
                        List<Reservation> seatReservations = reservations.stream()
                            .filter(r -> r.getSeatId().equals(seat.getId()) && !"CANCELLED".equals(r.getStatus()))
                            .collect(Collectors.toList());
                        
                        // 如果没有预约，整个开放时间段都可用
                        if (seatReservations.isEmpty()) {
                            return List.of(Map.of(
                                "startTime", studyRoom.getOpenTime(),
                                "endTime", studyRoom.getCloseTime()
                            ));
                        }
                        
                        // 计算可用时间段
                        List<Map<String, String>> availableSlots = new java.util.ArrayList<>();
                        
                        // 按开始时间排序预约
                        seatReservations.sort((r1, r2) -> r1.getStartTime().compareTo(r2.getStartTime()));
                        
                        // 检查开放时间到第一个预约之间的间隔
                        if (seatReservations.get(0).getStartTime().isAfter(LocalTime.parse(studyRoom.getOpenTime()))) {
                            availableSlots.add(Map.of(
                                "startTime", LocalTime.parse(studyRoom.getOpenTime()).format(DateTimeFormatter.ofPattern("HH:mm")),
                                "endTime", seatReservations.get(0).getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            ));
                        }
                        
                        // 检查相邻预约之间的间隔
                        for (int i = 0; i < seatReservations.size() - 1; i++) {
                            LocalTime currentEndTime = seatReservations.get(i).getEndTime();
                            LocalTime nextStartTime = seatReservations.get(i + 1).getStartTime();
                            
                            if (currentEndTime.isBefore(nextStartTime)) {
                                availableSlots.add(Map.of(
                                    "startTime", currentEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    "endTime", nextStartTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                ));
                            }
                        }
                        
                        // 检查最后一个预约到关闭时间之间的间隔
                        LocalTime lastEndTime = seatReservations.get(seatReservations.size() - 1).getEndTime();
                        if (lastEndTime.isBefore(LocalTime.parse(studyRoom.getCloseTime()))) {
                            availableSlots.add(Map.of(
                                "startTime", lastEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                "endTime", LocalTime.parse(studyRoom.getCloseTime()).format(DateTimeFormatter.ofPattern("HH:mm"))
                            ));
                        }
                        
                        return availableSlots;
                    }
                ));
            
            // 计算可用座位数
            long availableSeats = seats.stream()
                .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                .count();
            
            // 构建响应
            return ResponseEntity.ok(Map.of(
                "studyRoomId", studyRoomId,
                "studyRoomName", studyRoom.getName(),
                "date", date.toString(),
                "openTime", studyRoom.getOpenTime(),
                "closeTime", studyRoom.getCloseTime(),
                "totalSeats", seats.size(),
                "availableSeats", availableSeats,
                "seatAvailability", seatAvailability
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取可用时间段失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getStudyRoomStatus(String studyRoomId, LocalDate date, String startTime, String endTime) {
        try {
            // 验证自习室是否存在
            StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室不存在"));
            }
            
            // 首先检查自习室本身的状态，如果不可用，直接返回其状态
            if (!"AVAILABLE".equals(studyRoom.getStatus())) {
                return ResponseEntity.ok(Map.of(
                    "status", studyRoom.getStatus(),
                    "message", "自习室当前不可用，状态为：" + studyRoom.getStatus()
                ));
            }

            // 获取自习室的所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            if (seats.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "EMPTY",
                    "message", "自习室没有座位"
                ));
            }

            // 解析时间
            LocalTime now = LocalTime.now();
            LocalTime queryStartTime = startTime != null ? 
                LocalTime.parse(startTime) : 
                now.withMinute(0).withSecond(0);
            LocalTime queryEndTime = endTime != null ? 
                LocalTime.parse(endTime) : 
                queryStartTime.plusHours(1);

            // 查询该时间段内的预约
            List<Reservation> reservations = reservationRepository.findByStudyRoomIdAndTimeRange(
                studyRoomId, date, queryStartTime, queryEndTime);

            // 统计已预约的座位数
            long reservedSeats = reservations.stream()
                .filter(r -> r.getStatus().equals("CONFIRMED"))
                .count();

            // 判断自习室状态
            String status;
            if (reservedSeats == 0) {
                status = "EMPTY";
            } else if (reservedSeats == seats.size()) {
                status = "FULL";
            } else {
                status = "AVAILABLE";
            }

            return ResponseEntity.ok(Map.of(
                "status", status,
                "totalSeats", seats.size(),
                "reservedSeats", reservedSeats,
                "availableSeats", seats.size() - reservedSeats,
                "startTime", queryStartTime,
                "endTime", queryEndTime
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室状态失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getStudyRoomSeatsStatus(String studyRoomId, LocalDate date, String startTime, String endTime) {
        try {
            // 验证自习室是否存在
            StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室不存在"));
            }

            // 获取自习室的所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            if (seats.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "自习室没有座位",
                    "seats", Collections.emptyList()
                ));
            }

            // 解析时间
            LocalTime now = LocalTime.now();
            LocalTime queryStartTime = startTime != null ? 
                LocalTime.parse(startTime) : 
                now.withMinute(0).withSecond(0);
            LocalTime queryEndTime = endTime != null ? 
                LocalTime.parse(endTime) : 
                queryStartTime.plusHours(1);

            // 查询该时间段内的预约
            List<Reservation> reservations = reservationRepository.findByStudyRoomIdAndTimeRange(
                studyRoomId, date, queryStartTime, queryEndTime);

            // 构建座位状态列表
            List<Map<String, Object>> seatStatusList = seats.stream()
                .map(seat -> {
                    Map<String, Object> seatStatus = new HashMap<>();
                    seatStatus.put("seatId", seat.getId());
                    seatStatus.put("seatNumber", seat.getSeatNumber());
                    
                    // 首先检查座位物理状态，如果不可用，直接返回物理状态
                    if (!"AVAILABLE".equals(seat.getStatus())) {
                        seatStatus.put("status", seat.getStatus());
                        seatStatus.put("reservationId", null);
                        return seatStatus;
                    }
                    
                    // 查找该座位的预约
                    Optional<Reservation> reservation = reservations.stream()
                        .filter(r -> r.getSeatId().equals(seat.getId()))
                        .findFirst();

                    String status = reservation.map(Reservation::getStatus)
                        .orElse("AVAILABLE");

                    seatStatus.put("status", status);
                    seatStatus.put("reservationId", reservation.map(Reservation::getId).orElse(null));
                    return seatStatus;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "studyRoomId", studyRoomId,
                "studyRoomName", studyRoom.getName(),
                "date", date.toString(),
                "startTime", queryStartTime,
                "endTime", queryEndTime,
                "seats", seatStatusList
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取座位状态失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getStudyRoomsStatus(LocalDate date, String startTime, String endTime) {
        try {
            // 获取所有自习室
            List<StudyRoom> studyRooms = studyRoomRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 解析时间
            LocalTime start = startTime != null ? LocalTime.parse(startTime) : LocalTime.now();
            LocalTime end = endTime != null ? LocalTime.parse(endTime) : start.plusHours(1);
            
            // 检查时间是否合法
            if (end.isBefore(start)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "结束时间不能早于开始时间"));
            }
            
            for (StudyRoom room : studyRooms) {
                Map<String, Object> roomStatus = new HashMap<>();
                roomStatus.put("id", room.getId());
                roomStatus.put("name", room.getName());
                roomStatus.put("location", room.getLocation());
                roomStatus.put("openTime", room.getOpenTime());
                roomStatus.put("closeTime", room.getCloseTime());
                roomStatus.put("imageUrl", room.getImageUrl());
                
                // 首先检查自习室本身的状态，如果不可用，直接返回其状态
                if (!"AVAILABLE".equals(room.getStatus())) {
                    roomStatus.put("status", room.getStatus());
                    result.add(roomStatus);
                    continue;
                }
                
                // 检查时间段是否在开放时间内
                LocalTime openTime = LocalTime.parse(room.getOpenTime());
                LocalTime closeTime = LocalTime.parse(room.getCloseTime());
                
                if (start.isBefore(openTime) || end.isAfter(closeTime)) {
                    // 如果时间段不在开放时间内，设置状态为未开放
                    roomStatus.put("status", "CLOSED");
                    result.add(roomStatus);
                    continue;
                }
                
                // 获取该自习室的所有座位
                List<Seat> seats = seatRepository.findByStudyRoomId(room.getId());
                
                // 统计已预约的座位数
                long reservedSeats = seats.stream()
                    .filter(seat -> reservationRepository.existsBySeatIdAndDateAndTimeRange(
                        seat.getId(), date, start, end))
                    .count();
                
                // 根据预约情况设置自习室状态
                if (reservedSeats == seats.size()) {
                    roomStatus.put("status", "FULL");
                } else {
                    roomStatus.put("status", "AVAILABLE");
                }
                
                result.add(roomStatus);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取自习室状态失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室状态失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getStudyRoomDetail(String studyRoomId, LocalDate date, String startTime, String endTime) {
        try {
            // 获取自习室信息
            StudyRoom room = studyRoomRepository.findById(studyRoomId)
                .orElseThrow(() -> new RuntimeException("自习室不存在"));
                
            // 构建自习室详情
            Map<String, Object> studyRoom = new HashMap<>();
            studyRoom.put("id", room.getId());
            studyRoom.put("name", room.getName());
            studyRoom.put("location", room.getLocation());
            studyRoom.put("description", room.getDescription());
            studyRoom.put("imageUrl", room.getImageUrl());
            studyRoom.put("openTime", room.getOpenTime());
            studyRoom.put("closeTime", room.getCloseTime());
            studyRoom.put("maxAdvanceDays", room.getMaxAdvanceDays());
            studyRoom.put("physicalStatus", room.getStatus());  // 单独保存物理状态
            
            // 解析时间
            LocalTime now = LocalTime.now();
            LocalTime queryStartTime = startTime != null ? 
                LocalTime.parse(startTime) : 
                now.withMinute(0).withSecond(0);
            LocalTime queryEndTime = endTime != null ? 
                LocalTime.parse(endTime) : 
                queryStartTime.plusHours(1);
            
            // 获取座位信息
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            studyRoom.put("totalSeats", seats.size());
            
            // 获取预约信息
            List<Reservation> reservations = reservationRepository
                .findByStudyRoomIdAndTimeRange(studyRoomId, date, queryStartTime, queryEndTime);
            
            // 计算物理状态可用的座位数量
            long physicallyAvailableSeats = seats.stream()
                .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                .count();
            
            // 计算已预约的座位数（在物理可用的座位中）
            long reservedSeats = reservations.stream()
                .filter(r -> r.getStatus().equals("CONFIRMED"))
                .filter(r -> seats.stream()
                    .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                    .anyMatch(seat -> seat.getId().equals(r.getSeatId())))
                .count();
            
            long availableSeats = physicallyAvailableSeats - reservedSeats;
            studyRoom.put("availableSeats", availableSeats);
            studyRoom.put("physicallyAvailableSeats", physicallyAvailableSeats);
            
            // 设置状态
            String status;
            if (!"AVAILABLE".equals(room.getStatus())) {
                // 如果自习室物理状态不可用，直接使用物理状态
                status = room.getStatus();
            } else if (physicallyAvailableSeats == 0) {
                // 如果没有物理可用的座位
                status = "NO_AVAILABLE_SEATS";
            } else if (availableSeats == 0) {
                // 如果所有物理可用的座位都被预约
                status = "FULL";
            } else if (reservedSeats == 0) {
                // 如果没有被预约的座位
                status = "EMPTY";
            } else {
                // 有一些座位被预约，但还有可用座位
                status = "AVAILABLE";
            }
            studyRoom.put("status", status);
            
            // 获取座位状态列表
            List<Map<String, Object>> seatList = seats.stream()
                .map(seat -> {
                    Map<String, Object> seatInfo = new HashMap<>();
                    seatInfo.put("id", seat.getId());
                    seatInfo.put("seatNumber", seat.getSeatNumber());
                    seatInfo.put("physicalStatus", seat.getStatus());  // 添加物理状态
                    
                    // 首先检查座位物理状态
                    if (!"AVAILABLE".equals(seat.getStatus())) {
                        seatInfo.put("status", seat.getStatus());
                        seatInfo.put("reservationId", null);
                        return seatInfo;
                    }
                    
                    // 查找该座位的预约
                    Optional<Reservation> reservation = reservations.stream()
                        .filter(r -> r.getSeatId().equals(seat.getId()) && 
                                   r.getStatus().equals("CONFIRMED"))
                        .findFirst();
                    
                    seatInfo.put("status", reservation.isPresent() ? "OCCUPIED" : "AVAILABLE");
                    seatInfo.put("reservationId", reservation.map(Reservation::getId).orElse(null));
                    
                    return seatInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "studyRoom", studyRoom,
                "seats", seatList
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室详情失败: " + e.getMessage()));
        }
    }

    /**
     * 判断两个时间段是否重叠
     * 
     * @param start1 第一个时间段的开始时间
     * @param end1 第一个时间段的结束时间
     * @param start2 第二个时间段的开始时间
     * @param end2 第二个时间段的结束时间
     * @return 是否重叠
     */
    private boolean isTimeOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return (start1.isBefore(end2) && start2.isBefore(end1));
    }
} 