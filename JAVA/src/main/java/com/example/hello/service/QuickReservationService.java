package com.example.hello.service;

import com.example.hello.dto.QuickReserveRequest;
import com.example.hello.model.dto.ReservationDTO;

public interface QuickReservationService {
    /**
     * 快速预约
     * @param request 预约请求
     * @return 预约信息
     */
    ReservationDTO quickReserve(QuickReserveRequest request);
} 