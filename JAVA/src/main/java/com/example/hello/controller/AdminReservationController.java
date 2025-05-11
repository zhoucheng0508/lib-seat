package com.example.hello.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.dto.AdminReservationDTO;
import com.example.hello.exception.ResourceNotFoundException;
import com.example.hello.model.ReservationStatus;
import com.example.hello.model.entity.Reservation;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.service.AdminReservationService;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    @Autowired
    private AdminReservationService adminReservationService;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    /**
     * 分页查询预约记录
     */
    @GetMapping
    public ResponseEntity<?> getReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String seatId,
            @RequestParam(required = false) String studyRoomId,
            @RequestParam(defaultValue = "date,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
            
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<AdminReservationDTO> reservations = adminReservationService.getReservations(
            userId, startDate, endDate, status, seatId, studyRoomId, pageRequest);
            
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * 删除预约记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(
            @PathVariable String id,
            @RequestHeader("X-Admin-ID") String adminId) {
        try {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
                    
            if (ReservationStatus.CHECKED_IN.name().equals(reservation.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "已签到的预约不能删除"));
            }
            
            adminReservationService.deleteReservation(id, adminId);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 调整预约状态
     */
    @PutMapping("/{id}/adjust-status")
    public ResponseEntity<?> adjustReservationStatus(
            @PathVariable String id,
            @RequestHeader("X-Admin-ID") String adminId,
            @RequestBody Map<String, String> request) {
        try {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
                    
            if (!ReservationStatus.NO_SHOW.name().equals(reservation.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "只能调整未签到的预约状态"));
            }
            
            adminReservationService.adjustReservationStatus(id, adminId);
            return ResponseEntity.ok(Map.of("message", "状态调整成功"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 