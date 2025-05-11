package com.example.hello.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.dto.QuickReserveRequest;
import com.example.hello.exception.BusinessException;
import com.example.hello.model.dto.ReservationDTO;
import com.example.hello.model.entity.Reservation;
import com.example.hello.model.entity.Seat;
import com.example.hello.model.entity.StudyRoom;
import com.example.hello.model.entity.User;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.repository.StudyRoomRepository;
import com.example.hello.repository.UserRepository;
import com.example.hello.service.QuickReservationService;
import com.example.hello.service.ReservationService;

@Service
public class QuickReservationServiceImpl implements QuickReservationService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Autowired
    private ReservationService reservationService;

    @Override
    @Transactional
    public ReservationDTO quickReserve(QuickReserveRequest request) {
        // 1. 验证用户是否存在
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new BusinessException("用户不存在"));

        // 2. 验证时间格式和合理性
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());
        if (startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        // 3. 检查用户在指定时间段是否有未取消的预约
        List<Reservation> overlappingReservations = reservationRepository.findUserOverlappingReservations(
            request.getUserId(),
            LocalDate.parse(request.getDate()),
            startTime,
            endTime);
        
        if (!overlappingReservations.isEmpty()) {
            throw new BusinessException("您在该时间段已有预约");
        }

        // 4. 获取所有可用的自习室
        List<StudyRoom> availableStudyRooms = studyRoomRepository.findByStatus("AVAILABLE");
        if (availableStudyRooms.isEmpty()) {
            throw new BusinessException("当前没有可用的自习室");
        }

        // 5. 遍历自习室，查找第一个可用座位
        Seat availableSeat = null;
        String studyRoomId = null;
        StudyRoom selectedStudyRoom = null;

        for (StudyRoom studyRoom : availableStudyRooms) {
            // 检查自习室开放时间
            LocalTime openTime = LocalTime.parse(studyRoom.getOpenTime());
            LocalTime closeTime = LocalTime.parse(studyRoom.getCloseTime());
            if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                continue;
            }

            // 查找空闲座位
            List<Seat> availableSeats = seatRepository.findByStudyRoomIdAndStatus(
                studyRoom.getId(), "AVAILABLE");
            
            if (!availableSeats.isEmpty()) {
                availableSeat = availableSeats.get(0);
                studyRoomId = studyRoom.getId();
                selectedStudyRoom = studyRoom;
                break;
            }
        }

        if (availableSeat == null) {
            throw new BusinessException("当前没有可用座位");
        }

        // 6. 检查座位是否已被预约
        boolean isSeatReserved = reservationRepository.existsBySeatIdAndDateAndTimeRange(
            availableSeat.getId(),
            LocalDate.parse(request.getDate()),
            startTime,
            endTime);
        
        if (isSeatReserved) {
            throw new BusinessException("该座位已被预约");
        }

        // 7. 使用已有的预约功能创建预约
        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setStudyRoomId(studyRoomId);
        reservation.setSeatId(availableSeat.getId());
        reservation.setDate(LocalDate.parse(request.getDate()));
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus("CONFIRMED");
        reservation.setCreatedAt(LocalDateTime.now());

        ResponseEntity<?> response = reservationService.createReservation(reservation);

        if (response.getStatusCode().is2xxSuccessful()) {
            return (ReservationDTO) response.getBody();
        } else {
            throw new BusinessException(((Map<String, String>) response.getBody()).get("message"));
        }
    }
} 