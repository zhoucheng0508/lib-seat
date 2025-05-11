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
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.example.hello.model.dto.SeatDTO;
import com.example.hello.model.dto.StudyRoomDTO;
import com.example.hello.model.entity.Reservation;
import com.example.hello.model.entity.Seat;
import com.example.hello.model.entity.StudyRoom;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.repository.StudyRoomRepository;
import com.example.hello.service.SeatService;
import com.example.hello.service.StudyRoomService;

/**
 * 自习室服务实现类
 * 提供自习室相关的业务逻辑处理，包括自习室的增删改查等功能
 */
@Service
public class StudyRoomServiceImpl implements StudyRoomService {

    /**
     * 自习室数据访问对象
     */
    @Autowired
    private StudyRoomRepository studyRoomRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private SeatService seatService;
    
    /**
     * 将自习室实体对象转换为数据传输对象
     * 
     * @param room 自习室实体对象
     * @return 自习室DTO对象
     */
    @Override
    public StudyRoomDTO convertToDTO(StudyRoom room) {
        StudyRoomDTO dto = new StudyRoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setLocation(room.getLocation());
        dto.setCapacity(room.getCapacity());
        dto.setDescription(room.getDescription());
        dto.setStatus(room.getStatus());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setOpenTime(room.getOpenTime());
        dto.setCloseTime(room.getCloseTime());
        dto.setMaxAdvanceDays(room.getMaxAdvanceDays());
        dto.setImageUrl(room.getImageUrl());
        
        // 获取自习室的所有座位
        List<Seat> seats = seatRepository.findByStudyRoomId(room.getId());
        List<SeatDTO> seatDTOs = seats.stream()
            .map(seatService::convertToDTO)
            .collect(Collectors.toList());
        dto.setSeats(seatDTOs);
        
        return dto;
    }
    
    /**
     * 获取所有自习室信息
     * 
     * @return 包含所有自习室信息的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> getAllStudyRooms() {
        try {
            // 查询所有自习室数据
            List<StudyRoom> rooms = studyRoomRepository.findAll();
            // 将实体对象集合转换为DTO对象集合
            List<StudyRoomDTO> roomDTOs = rooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(roomDTOs);
        } catch (Exception e) {
            // 发生异常时返回错误信息
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建新的自习室
     * 
     * @param room 自习室实体对象
     * @return 创建结果的ResponseEntity对象
     */
    @Override
    @Transactional
    public ResponseEntity<?> createStudyRoom(StudyRoom room) {
        try {
            // 验证自习室信息
            if (room.getCapacity() == null || room.getCapacity() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室容量必须大于0"));
            }
            
            if (room.getName() == null || room.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "自习室名称不能为空"));
            }
            
            if (room.getOpenTime() == null || room.getCloseTime() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "开放时间和关闭时间不能为空"));
            }
            
            // 保存自习室信息
            StudyRoom savedRoom = studyRoomRepository.save(room);
            
            // 确保自习室ID已生成
            if (savedRoom.getId() == null) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseEntity.internalServerError()
                    .body(Map.of("message", "自习室ID生成失败"));
            }
            
            // 创建座位
            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= room.getCapacity(); i++) {
                Seat seat = new Seat();
                seat.setSeatNumber(String.format("%03d", i));
                seat.setStudyRoomId(savedRoom.getId());
                seat.setStatus("AVAILABLE");
                seat.setCreatedAt(System.currentTimeMillis());
                seats.add(seat);
            }
            
            // 批量保存座位
            try {
                seatRepository.saveAll(seats);
            } catch (Exception e) {
                // 如果座位创建失败，标记事务回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                System.err.println("创建座位失败: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "创建座位失败: " + e.getMessage()));
            }
            
            return ResponseEntity.ok(convertToDTO(savedRoom));
        } catch (Exception e) {
            // 记录详细的错误信息并标记事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.err.println("创建自习室失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "message", "创建自习室失败: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
                ));
        }
    }
    
    /**
     * 根据ID获取自习室信息
     * 
     * @param id 自习室ID
     * @return 自习室信息的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> getStudyRoom(String id) {
        try {
            // 根据ID查找自习室，找到则返回DTO对象，否则返回404
            return studyRoomRepository.findById(id)
                .map(room -> ResponseEntity.ok(convertToDTO(room)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            // 发生异常时返回错误信息
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新自习室信息
     * 
     * @param id 自习室ID
     * @param room 包含更新内容的自习室对象
     * @return 更新结果的ResponseEntity对象
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateStudyRoom(String id, StudyRoom room) {
        try {
            return studyRoomRepository.findById(id)
                .map(existingRoom -> {
                    // 更新基本信息
                    if (room.getName() != null) {
                        existingRoom.setName(room.getName());
                    }
                    if (room.getLocation() != null) {
                        existingRoom.setLocation(room.getLocation());
                    }
                    if (room.getDescription() != null) {
                        existingRoom.setDescription(room.getDescription());
                    }
                    if (room.getOpenTime() != null) {
                        existingRoom.setOpenTime(room.getOpenTime());
                    }
                    if (room.getCloseTime() != null) {
                        existingRoom.setCloseTime(room.getCloseTime());
                    }
                    if (room.getMaxAdvanceDays() != null) {
                        existingRoom.setMaxAdvanceDays(room.getMaxAdvanceDays());
                    }
                    if (room.getStatus() != null) {
                        existingRoom.setStatus(room.getStatus());
                    }
                    if (room.getImageUrl() != null) {
                        existingRoom.setImageUrl(room.getImageUrl());
                    }
                    
                    // 处理容量变化
                    if (room.getCapacity() != null && !room.getCapacity().equals(existingRoom.getCapacity())) {
                        int oldCapacity = existingRoom.getCapacity();
                        int newCapacity = room.getCapacity();
                        
                        if (newCapacity > oldCapacity) {
                            // 增加座位
                            List<Seat> newSeats = new ArrayList<>();
                            for (int i = oldCapacity + 1; i <= newCapacity; i++) {
                                Seat seat = new Seat();
                                seat.setSeatNumber(String.format("%03d", i));
                                seat.setStudyRoomId(id);
                                seat.setStatus("AVAILABLE");
                                newSeats.add(seat);
                            }
                            seatRepository.saveAll(newSeats);
                        } else if (newCapacity < oldCapacity) {
                            // 减少座位
                            // 先检查要删除的座位是否有预约
                            List<Seat> seatsToDelete = seatRepository.findByStudyRoomIdAndSeatNumberGreaterThan(
                                id, String.format("%03d", newCapacity));
                            
                            // 检查这些座位是否有预约
                            for (Seat seat : seatsToDelete) {
                                if (reservationRepository.existsBySeatId(seat.getId())) {
                                    return ResponseEntity.badRequest()
                                        .body(Map.of("message", 
                                            "座位 " + seat.getSeatNumber() + " 有预约记录，无法删除"));
                                }
                            }
                            
                            // 删除座位
                            seatRepository.deleteAll(seatsToDelete);
                        }
                        
                        existingRoom.setCapacity(newCapacity);
                    }
                    
                    return ResponseEntity.ok(convertToDTO(studyRoomRepository.save(existingRoom)));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "更新自习室失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新自习室状态
     * 
     * @param id 自习室ID
     * @param status 包含状态信息的Map
     * @return 更新结果的ResponseEntity对象
     */
    @Override
    public ResponseEntity<?> updateStudyRoomStatus(String id, Map<String, String> status) {
        try {
            // 根据ID查找自习室，并更新其状态
            return studyRoomRepository.findById(id)
                .map(room -> {
                    room.setStatus(status.get("status"));
                    return ResponseEntity.ok(convertToDTO(studyRoomRepository.save(room)));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            // 发生异常时返回错误信息
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "更新自习室状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除自习室
     * 
     * @param id 自习室ID
     * @return 删除结果的ResponseEntity对象
     */
    @Override
    @Transactional
    public ResponseEntity<?> deleteStudyRoom(String id) {
        try {
            return studyRoomRepository.findById(id)
                .map(room -> {
                    // 检查该自习室的座位是否有预约
                    List<Seat> seats = seatRepository.findByStudyRoomId(id);
                    for (Seat seat : seats) {
                        if (reservationRepository.existsBySeatId(seat.getId())) {
                            return ResponseEntity.badRequest()
                                .body(Map.of("message", 
                                    "座位 " + seat.getSeatNumber() + " 有预约记录，无法删除自习室"));
                        }
                    }
                    
                    // 删除所有座位
                    seatRepository.deleteAll(seats);
                    
                    // 删除自习室
                    studyRoomRepository.delete(room);
                    
                    return ResponseEntity.ok(Map.of("message", "自习室删除成功"));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "删除自习室失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取自习室所有座位在指定日期的实时状态
     */
    @Override
    public ResponseEntity<?> getStudyRoomSeatsRealTimeStatus(String studyRoomId, LocalDate date) {
        try {
            // 获取自习室信息
            StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取当前时间
            LocalTime currentTime = LocalTime.now();
            
            // 获取自习室营业时间
            LocalTime openTime = LocalTime.parse(studyRoom.getOpenTime());
            LocalTime closeTime = LocalTime.parse(studyRoom.getCloseTime());
            
            // 获取自习室所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            
            // 获取当天该自习室的所有预约
            List<Reservation> studyRoomReservations = 
                reservationRepository.findByStudyRoomIdAndDate(studyRoomId, date);
            
            // 计算每个座位的状态
            List<Map<String, Object>> seatsStatus = calculateSeatsStatus(
                seats, studyRoom, studyRoomReservations, date, currentTime, openTime, closeTime);
            
            // 统计可用座位数
            long availableSeats = seatsStatus.stream()
                .filter(status -> "AVAILABLE".equals(status.get("currentStatus")))
                .count();
            
            // 构建最终结果
            Map<String, Object> result = new HashMap<>();
            result.put("studyRoomId", studyRoom.getId());
            result.put("studyRoomName", studyRoom.getName());
            result.put("date", date.toString());
            result.put("openTime", studyRoom.getOpenTime());
            result.put("closeTime", studyRoom.getCloseTime());
            result.put("totalSeats", seats.size());
            result.put("availableSeats", availableSeats);
            result.put("seatsStatus", seatsStatus);
            
            return ResponseEntity.ok(result);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室座位实时状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取自习室所有座位在指定时间段的状态
     */
    @Override
    public ResponseEntity<?> getStudyRoomSeatsStatusForTimeSlot(String studyRoomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            // 获取自习室信息
            StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId).orElse(null);
            if (studyRoom == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取自习室营业时间
            LocalTime openTime = LocalTime.parse(studyRoom.getOpenTime());
            LocalTime closeTime = LocalTime.parse(studyRoom.getCloseTime());
            
            // 检查请求的时间段是否在自习室开放时间内
            if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "请求的时间段不在自习室开放时间内",
                    "openTime", studyRoom.getOpenTime(),
                    "closeTime", studyRoom.getCloseTime()
                ));
            }
            
            // 获取自习室所有座位
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            
            // 获取当天该自习室的所有预约
            List<Reservation> studyRoomReservations = 
                reservationRepository.findByStudyRoomIdAndDate(studyRoomId, date);
            
            // 计算每个座位在指定时间段内的状态
            List<Map<String, Object>> seatsStatus = new ArrayList<>();
            
            for (Seat seat : seats) {
                Map<String, Object> seatStatus = new HashMap<>();
                seatStatus.put("seatId", seat.getId());
                seatStatus.put("seatNumber", seat.getSeatNumber());
                seatStatus.put("physicalStatus", seat.getStatus());
                
                // 若座位物理状态不可用，直接标记为不可用
                if (!"AVAILABLE".equals(seat.getStatus())) {
                    seatStatus.put("currentStatus", "UNAVAILABLE");
                    seatStatus.put("message", "座位物理状态不可用");
                    seatsStatus.add(seatStatus);
                    continue;
                }
                
                // 检查该座位在指定时间段是否已被预约
                boolean isReserved = false;
                List<Reservation> overlappingReservations = new ArrayList<>();
                
                for (Reservation reservation : studyRoomReservations) {
                    if (reservation.getSeatId().equals(seat.getId()) && 
                        !"CANCELLED".equals(reservation.getStatus())) {
                        
                        // 检查时间段是否重叠
                        boolean hasOverlap = !(
                            reservation.getEndTime().isBefore(startTime) || 
                            reservation.getStartTime().isAfter(endTime)
                        );
                        
                        if (hasOverlap) {
                            isReserved = true;
                            overlappingReservations.add(reservation);
                        }
                    }
                }
                
                if (isReserved) {
                    seatStatus.put("currentStatus", "RESERVED");
                    seatStatus.put("message", "该时间段已被预约");
                    
                    // 添加冲突的预约信息
                    if (!overlappingReservations.isEmpty()) {
                        seatStatus.put("overlappingReservations", overlappingReservations.stream()
                            .map(r -> Map.of(
                                "id", r.getId(),
                                "userId", r.getUserId(),
                                "startTime", r.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                "endTime", r.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                "status", r.getStatus()
                            ))
                            .collect(Collectors.toList()));
                    }
                } else {
                    seatStatus.put("currentStatus", "AVAILABLE");
                    seatStatus.put("message", "该时间段座位可预约");
                }
                
                seatsStatus.add(seatStatus);
            }
            
            // 统计可用座位数
            long availableSeats = seatsStatus.stream()
                .filter(status -> (boolean) status.getOrDefault("available", false))
                .count();
            
            // 构建最终结果
            Map<String, Object> result = new HashMap<>();
            result.put("studyRoomId", studyRoom.getId());
            result.put("studyRoomName", studyRoom.getName());
            result.put("date", date.toString());
            result.put("startTime", startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            result.put("endTime", endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            result.put("openTime", studyRoom.getOpenTime());
            result.put("closeTime", studyRoom.getCloseTime());
            result.put("totalSeats", seats.size());
            result.put("availableSeats", availableSeats);
            result.put("seatsStatus", seatsStatus);
            
            return ResponseEntity.ok(result);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取自习室座位时间段状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 计算所有座位的状态（私有辅助方法）
     */
    private List<Map<String, Object>> calculateSeatsStatus(
            List<Seat> seats, StudyRoom studyRoom, List<Reservation> studyRoomReservations,
            LocalDate date, LocalTime currentTime, LocalTime openTime, LocalTime closeTime) {
        
        return seats.stream()
            .map(seat -> {
                Map<String, Object> seatStatus = new HashMap<>();
                seatStatus.put("seatId", seat.getId());
                seatStatus.put("seatNumber", seat.getSeatNumber());
                seatStatus.put("physicalStatus", seat.getStatus());
                
                // 若座位物理状态不可用，直接返回
                if (!"AVAILABLE".equals(seat.getStatus())) {
                    seatStatus.put("currentStatus", "UNAVAILABLE");
                    seatStatus.put("message", "座位物理状态不可用");
                    return seatStatus;
                }
                
                // 检查当前是否在营业时间内
                boolean withinBusinessHours = !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
                if (!withinBusinessHours && date.isEqual(LocalDate.now())) {
                    seatStatus.put("currentStatus", "CLOSED");
                    seatStatus.put("message", "当前不在自习室开放时间内");
                    return seatStatus;
                }
                
                // 获取该座位当天的所有预约
                List<Reservation> seatReservations = studyRoomReservations.stream()
                    .filter(r -> r.getSeatId().equals(seat.getId()) && !"CANCELLED".equals(r.getStatus()))
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
                    seatStatus.put("currentStatus", "OCCUPIED");
                    seatStatus.put("message", "座位当前已被预约");
                    Map<String, String> reservationInfo = new HashMap<>();
                    reservationInfo.put("id", currentReservation.getId());
                    reservationInfo.put("userId", currentReservation.getUserId());
                    reservationInfo.put("startTime", currentReservation.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    reservationInfo.put("endTime", currentReservation.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    seatStatus.put("currentReservation", reservationInfo);
                } else {
                    seatStatus.put("currentStatus", "AVAILABLE");
                    seatStatus.put("message", "座位当前可用");
                }
                
                return seatStatus;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取自习室在指定日期的可用时间段
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @return 可用时间段列表
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
            
            // 验证预约日期是否在允许的提前预约天数范围内
            LocalDate today = LocalDate.now();
            LocalDate maxDate = today.plusDays(studyRoom.getMaxAdvanceDays());
            if (date.isBefore(today) || date.isAfter(maxDate)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "查询日期必须在当前日期到未来" + studyRoom.getMaxAdvanceDays() + "天内"));
            }
            
            // 获取自习室开放时间
            LocalTime openTime = LocalTime.parse(studyRoom.getOpenTime());
            LocalTime closeTime = LocalTime.parse(studyRoom.getCloseTime());
            
            // 获取自习室内所有座位
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
                        if (seatReservations.get(0).getStartTime().isAfter(openTime)) {
                            availableSlots.add(Map.of(
                                "startTime", openTime.format(DateTimeFormatter.ofPattern("HH:mm")),
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
                        if (lastEndTime.isBefore(closeTime)) {
                            availableSlots.add(Map.of(
                                "startTime", lastEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                "endTime", closeTime.format(DateTimeFormatter.ofPattern("HH:mm"))
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

    /**
     * 清理残留的座位数据
     * 删除所有没有对应自习室的座位
     */
    @Transactional
    public ResponseEntity<?> cleanOrphanedSeats() {
        try {
            // 获取所有自习室ID
            List<String> studyRoomIds = studyRoomRepository.findAll().stream()
                .map(StudyRoom::getId)
                .collect(Collectors.toList());
            
            // 获取所有座位
            List<Seat> allSeats = seatRepository.findAll();
            
            // 找出没有对应自习室的座位
            List<Seat> orphanedSeats = allSeats.stream()
                .filter(seat -> !studyRoomIds.contains(seat.getStudyRoomId()))
                .collect(Collectors.toList());
            
            if (orphanedSeats.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "没有发现残留的座位数据"));
            }
            
            // 删除这些座位
            seatRepository.deleteAll(orphanedSeats);
            
            return ResponseEntity.ok(Map.of(
                "message", "成功清理残留座位数据",
                "deletedSeats", orphanedSeats.size(),
                "deletedSeatIds", orphanedSeats.stream()
                    .map(Seat::getId)
                    .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "清理残留座位数据失败: " + e.getMessage()));
        }
    }
} 