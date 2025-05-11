package com.example.hello.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.model.dto.SeatDTO;
import com.example.hello.model.entity.Reservation;
import com.example.hello.model.entity.Seat;
import com.example.hello.model.entity.StudyRoom;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.repository.StudyRoomRepository;
import com.example.hello.service.SeatService;

/**
 * 座位服务实现类
 * 提供座位相关的业务逻辑处理
 */
@Service
public class SeatServiceImpl implements SeatService {

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
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    /**
     * 将座位实体对象转换为DTO对象
     * 
     * @param seat 座位实体对象
     * @return 座位DTO对象
     */
    @Override
    public SeatDTO convertToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setStudyRoomId(seat.getStudyRoomId());
        dto.setStatus(seat.getStatus());
        
        // 获取关联的自习室名称
        studyRoomRepository.findById(seat.getStudyRoomId())
            .ifPresent(room -> dto.setStudyRoomName(room.getName()));
        
        return dto;
    }
    
    /**
     * 获取自习室的所有座位
     * 
     * @param studyRoomId 自习室ID
     * @return 座位列表的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> getSeatsByStudyRoom(String studyRoomId) {
        try {
            // 先检查自习室是否存在
            if (!studyRoomRepository.existsById(studyRoomId)) {
                return ResponseEntity.notFound().build();
            }
            
            // 查询自习室的所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            List<SeatDTO> seatDTOs = seats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(seatDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室座位列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建新座位
     * 
     * @param seat 座位实体对象
     * @return 创建结果的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> createSeat(Seat seat) {
        try {
            // 验证自习室是否存在
            if (!studyRoomRepository.existsById(seat.getStudyRoomId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室不存在"));
            }
            
            // 检查座位号是否已存在
            if (seatRepository.findByStudyRoomIdAndSeatNumber(
                    seat.getStudyRoomId(), seat.getSeatNumber()) != null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "座位号已存在"));
            }
            
            // 保存座位
            Seat savedSeat = seatRepository.save(seat);
            return ResponseEntity.ok(convertToDTO(savedSeat));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "创建座位失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量创建座位
     * 
     * @param studyRoomId 自习室ID
     * @param request 包含座位数量等信息的请求参数
     * @return 创建结果的ResponseEntity对象
     */
    @Override
    @Transactional
    public ResponseEntity<?> createSeatsInBatch(String studyRoomId, Map<String, Object> request) {
        try {
            // 验证自习室是否存在
            StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室不存在"));
            }
            
            // 获取请求参数
            Integer count = (Integer) request.get("count");
            String prefix = (String) request.get("prefix");
            
            if (count == null || count <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "座位数量必须大于0"));
            }
            
            if (prefix == null) {
                prefix = ""; // 如果未提供前缀，则使用空字符串
            }
            
            // 批量创建座位
            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                String seatNumber = prefix + i;
                
                // 检查座位号是否已存在
                if (seatRepository.findByStudyRoomIdAndSeatNumber(studyRoomId, seatNumber) != null) {
                    continue; // 如果座位号已存在，则跳过
                }
                
                Seat seat = new Seat();
                seat.setSeatNumber(seatNumber);
                seat.setStudyRoomId(studyRoomId);
                seat.setStatus("AVAILABLE");
                seats.add(seat);
            }
            
            // 批量保存座位
            List<Seat> savedSeats = seatRepository.saveAll(seats);
            List<SeatDTO> seatDTOs = savedSeats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(Map.of(
                "message", "成功创建" + savedSeats.size() + "个座位",
                "seats", seatDTOs
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "批量创建座位失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取座位详情
     * 
     * @param id 座位ID
     * @return 座位信息的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> getSeat(String id) {
        try {
            return seatRepository.findById(id)
                .map(seat -> ResponseEntity.ok(convertToDTO(seat)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取座位信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新座位状态
     * 
     * @param id 座位ID
     * @param status 包含状态信息的Map
     * @return 更新结果的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> updateSeatStatus(String id, Map<String, String> status) {
        try {
            return seatRepository.findById(id)
                .map(seat -> {
                    String newStatus = status.get("status");
                    
                    // 验证状态值是否有效
                    if (!newStatus.equals("AVAILABLE") && 
                        !newStatus.equals("UNAVAILABLE") && 
                        !newStatus.equals("RESERVED")) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("message", "无效的座位状态"));
                    }
                    
                    seat.setStatus(newStatus);
                    return ResponseEntity.ok(convertToDTO(seatRepository.save(seat)));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "更新座位状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除座位
     * 
     * @param id 座位ID
     * @return 删除结果的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> deleteSeat(String id) {
        try {
            return seatRepository.findById(id)
                .map(seat -> {
                    seatRepository.delete(seat);
                    return ResponseEntity.ok(Map.of("message", "座位删除成功"));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "删除座位失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除自习室的所有座位
     * 
     * @param studyRoomId 自习室ID
     * @return 删除结果的ResponseEntity对象
     */
    @Override
    @Transactional
    public ResponseEntity<?> deleteAllSeatsByStudyRoom(String studyRoomId) {
        try {
            // 先检查自习室是否存在
            if (!studyRoomRepository.existsById(studyRoomId)) {
                return ResponseEntity.notFound().build();
            }
            
            // 查询自习室的所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            
            if (seats.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "该自习室没有座位"));
            }
            
            // 删除所有座位
            seatRepository.deleteAll(seats);
            
            return ResponseEntity.ok(Map.of(
                "message", "成功删除自习室的所有座位",
                "count", seats.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "删除自习室座位失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取座位在指定日期的实时状态
     */
    @Override
    public ResponseEntity<?> getSeatRealTimeStatus(String seatId, LocalDate date) {
        try {
            // 获取座位信息
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取当前时间
            LocalTime currentTime = LocalTime.now();
            
            // 获取座位所属自习室
            return studyRoomRepository.findById(seat.getStudyRoomId())
                .map(studyRoom -> {
                    // 获取自习室营业时间
                    LocalTime openTime = LocalTime.parse(studyRoom.getOpenTime());
                    LocalTime closeTime = LocalTime.parse(studyRoom.getCloseTime());
                    
                    // 计算座位状态
                    Map<String, Object> result = calculateSeatStatus(
                        seat, studyRoom, date, currentTime, openTime, closeTime, true);
                    
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "找不到座位对应的自习室")));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取座位实时状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取座位在指定时间段的状态
     */
    @Override
    public ResponseEntity<?> getSeatStatusForTimeSlot(String seatId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            // 获取座位信息
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取座位所属自习室
            return studyRoomRepository.findById(seat.getStudyRoomId())
                .map(studyRoom -> {
                    // 获取自习室营业时间
                    LocalTime openTime = LocalTime.parse(studyRoom.getOpenTime());
                    LocalTime closeTime = LocalTime.parse(studyRoom.getCloseTime());
                    
                    // 首先检查座位的物理状态
                    if (!"AVAILABLE".equals(seat.getStatus())) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("seatId", seat.getId());
                        result.put("seatNumber", seat.getSeatNumber());
                        result.put("studyRoomId", studyRoom.getId());
                        result.put("studyRoomName", studyRoom.getName());
                        result.put("date", date.toString());
                        result.put("startTime", startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                        result.put("endTime", endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                        result.put("physicalStatus", seat.getStatus());
                        result.put("available", false);
                        result.put("currentStatus", "UNAVAILABLE");
                        result.put("message", "座位物理状态不可用");
                        
                        return ResponseEntity.ok(result);
                    }
                    
                    // 检查请求的时间段是否在自习室开放时间内
                    if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("seatId", seat.getId());
                        result.put("seatNumber", seat.getSeatNumber());
                        result.put("studyRoomId", studyRoom.getId());
                        result.put("studyRoomName", studyRoom.getName());
                        result.put("date", date.toString());
                        result.put("startTime", startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                        result.put("endTime", endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                        result.put("physicalStatus", seat.getStatus());
                        result.put("available", false);
                        result.put("currentStatus", "CLOSED");
                        result.put("message", "请求的时间段不在自习室开放时间内");
                        result.put("openTime", studyRoom.getOpenTime());
                        result.put("closeTime", studyRoom.getCloseTime());
                        
                        return ResponseEntity.ok(result);
                    }
                    
                    // 检查时间段是否已被预约
                    List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                        seatId, date, startTime, endTime);
                        
                    if (!overlappingReservations.isEmpty()) {
                        // 过滤掉已取消的预约
                        List<Reservation> activeReservations = overlappingReservations.stream()
                            .filter(r -> !"CANCELLED".equals(r.getStatus()))
                            .collect(Collectors.toList());
                            
                        if (!activeReservations.isEmpty()) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("seatId", seat.getId());
                            result.put("seatNumber", seat.getSeatNumber());
                            result.put("studyRoomId", studyRoom.getId());
                            result.put("studyRoomName", studyRoom.getName());
                            result.put("date", date.toString());
                            result.put("startTime", startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                            result.put("endTime", endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                            result.put("physicalStatus", seat.getStatus());
                            result.put("available", false);
                            result.put("currentStatus", "RESERVED");
                            result.put("message", "该时间段已被预约");
                            
                            // 添加冲突的预约信息
                            List<Map<String, String>> conflicts = activeReservations.stream()
                                .map(r -> {
                                    Map<String, String> reservation = new HashMap<>();
                                    reservation.put("id", r.getId());
                                    reservation.put("userId", r.getUserId());
                                    reservation.put("startTime", r.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                                    reservation.put("endTime", r.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                                    reservation.put("status", r.getStatus());
                                    return reservation;
                                })
                                .collect(Collectors.toList());
                                
                            result.put("overlappingReservations", conflicts);
                            
                            return ResponseEntity.ok(result);
                        }
                    }
                    
                    // 时间段可用
                    Map<String, Object> result = new HashMap<>();
                    result.put("seatId", seat.getId());
                    result.put("seatNumber", seat.getSeatNumber());
                    result.put("studyRoomId", studyRoom.getId());
                    result.put("studyRoomName", studyRoom.getName());
                    result.put("date", date.toString());
                    result.put("startTime", startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                    result.put("endTime", endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                    result.put("physicalStatus", seat.getStatus());
                    result.put("available", true);
                    result.put("currentStatus", "AVAILABLE");
                    result.put("message", "该时间段座位可预约");
                    
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "找不到座位对应的自习室")));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取座位时间段状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 计算座位状态（私有辅助方法）
     * 
     * @param seat 座位对象
     * @param studyRoom 自习室对象
     * @param date 日期
     * @param currentTime 当前时间
     * @param openTime 开放时间
     * @param closeTime 关闭时间
     * @param includeTimeSlots 是否包含时间段信息
     * @return 座位状态信息
     */
    private Map<String, Object> calculateSeatStatus(
            Seat seat, StudyRoom studyRoom, LocalDate date, 
            LocalTime currentTime, LocalTime openTime, LocalTime closeTime,
            boolean includeTimeSlots) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("seatId", seat.getId());
        result.put("seatNumber", seat.getSeatNumber());
        result.put("studyRoomId", studyRoom.getId());
        result.put("studyRoomName", studyRoom.getName());
        result.put("date", date.toString());
        result.put("physicalStatus", seat.getStatus());
        result.put("openTime", studyRoom.getOpenTime());
        result.put("closeTime", studyRoom.getCloseTime());
        
        // 检查物理状态
        if (!"AVAILABLE".equals(seat.getStatus())) {
            result.put("currentStatus", "UNAVAILABLE");
            result.put("message", "座位物理状态不可用");
            return result;
        }
        
        // 检查当前是否在营业时间内
        boolean withinBusinessHours = !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
        if (!withinBusinessHours && date.isEqual(LocalDate.now())) {
            result.put("currentStatus", "CLOSED");
            result.put("message", "当前不在自习室开放时间内");
            return result;
        }
        
        // 获取该座位当天的所有预约
        List<Reservation> seatReservations = reservationRepository.findBySeatIdAndDate(seat.getId(), date)
            .stream()
            .filter(r -> !"CANCELLED".equals(r.getStatus()))
            .collect(Collectors.toList());
        
        // 检查当前是否有预约占用
        Reservation currentReservation = null;
        
        for (Reservation reservation : seatReservations) {
            if (!reservation.getEndTime().isBefore(currentTime) && 
                !reservation.getStartTime().isAfter(currentTime) &&
                date.isEqual(LocalDate.now())) {
                currentReservation = reservation;
                break;
            }
        }
        
        // 添加当前状态
        if (currentReservation != null) {
            result.put("currentStatus", "OCCUPIED");
            result.put("message", "座位当前已被预约");
            result.put("currentReservation", Map.of(
                "id", currentReservation.getId(),
                "userId", currentReservation.getUserId(),
                "startTime", currentReservation.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "endTime", currentReservation.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            ));
        } else {
            result.put("currentStatus", "AVAILABLE");
            result.put("message", "座位当前可用");
        }
        
        // 计算当日该座位的时间段状态
        if (includeTimeSlots && !seatReservations.isEmpty()) {
            result.put("reservedSlots", seatReservations.stream()
                .map(r -> Map.of(
                    "startTime", r.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    "endTime", r.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    "status", r.getStatus(),
                    "userId", r.getUserId()
                ))
                .collect(Collectors.toList()));
        }
        
        return result;
    }
} 