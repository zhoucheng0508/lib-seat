package com.example.hello.service.impl;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.dto.AdminReservationDTO;
import com.example.hello.exception.ResourceNotFoundException;
import com.example.hello.model.ReservationStatus;
import com.example.hello.model.entity.Reservation;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.service.AdminReservationService;

@Service
public class AdminReservationServiceImpl implements AdminReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Override
    public Page<AdminReservationDTO> getReservations(
            String userId,
            String startDate,
            String endDate,
            String status,
            String seatId,
            String studyRoomId,
            Pageable pageable) {
                
        LocalDate parsedStartDate = null;
        LocalDate parsedEndDate = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            parsedStartDate = LocalDate.parse(startDate);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            parsedEndDate = LocalDate.parse(endDate);
        }
        
        return reservationRepository.findByConditions(
            userId,
            parsedStartDate,
            parsedEndDate,
            status,
            seatId,
            studyRoomId,
            pageable);
    }
    
    @Override
    @Transactional
    public void deleteReservation(String id, String adminId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
                
        reservation.setIsDeleted(true);
        reservation.setDeletedBy(adminId);
        reservation.setDeletedAt(LocalDate.now());
        
        reservationRepository.save(reservation);
    }
    
    @Override
    @Transactional
    public void adjustReservationStatus(String id, String adminId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
                
        reservation.setStatus(ReservationStatus.CHECKED_IN.name());
        reservation.setAdjustedBy(adminId);
        reservation.setAdjustedAt(LocalDate.now());
        
        reservationRepository.save(reservation);
    }
} 