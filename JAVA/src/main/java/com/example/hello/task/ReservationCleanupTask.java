package com.example.hello.task;

import com.example.hello.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class ReservationCleanupTask {
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Scheduled(cron = "0 0 0 * * MON") // 每周一凌晨执行
    @Transactional
    public void cleanupDeletedReservations() {
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        reservationRepository.deleteByIsDeletedTrueAndDeletedAtBefore(oneWeekAgo);
    }
} 