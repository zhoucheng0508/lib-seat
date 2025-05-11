package com.example.hello.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.dto.QuickReserveRequest;
import com.example.hello.model.dto.ReservationDTO;
import com.example.hello.service.QuickReservationService;

@RestController
@RequestMapping("/api/reservations")
public class QuickReservationController {

    @Autowired
    private QuickReservationService quickReservationService;

    /**
     * 快速预约
     * 
     * @param request 预约请求
     * @return 预约结果
     */
    @PostMapping("/quick")
    public ResponseEntity<?> quickReserve(@RequestBody QuickReserveRequest request) {
        try {
            ReservationDTO reservation = quickReservationService.quickReserve(request);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
} 