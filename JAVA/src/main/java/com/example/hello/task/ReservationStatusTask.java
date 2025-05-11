package com.example.hello.task;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.hello.model.ReservationStatus;
import com.example.hello.model.entity.Reservation;
import com.example.hello.repository.ReservationRepository;

@Component
public class ReservationStatusTask {
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void updateReservationStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 只处理未取消的过期预约
        List<Reservation> expiredReservations = reservationRepository
            .findByDateAndEndTimeBeforeAndStatusIn(
                now.toLocalDate(),
                now.toLocalTime(),
                List.of(ReservationStatus.PENDING.name(), ReservationStatus.CHECKED_IN.name())
            );
            
        for (Reservation reservation : expiredReservations) {
            if (ReservationStatus.PENDING.name().equals(reservation.getStatus())) {
                reservation.setStatus(ReservationStatus.NO_SHOW.name());
            } else if (ReservationStatus.CHECKED_IN.name().equals(reservation.getStatus())) {
                reservation.setStatus(ReservationStatus.COMPLETED.name());
            }
        }
        
        reservationRepository.saveAll(expiredReservations);
    }
} 