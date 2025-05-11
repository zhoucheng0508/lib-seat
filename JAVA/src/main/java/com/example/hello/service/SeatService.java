package com.example.hello.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.example.hello.model.dto.SeatDTO;
import com.example.hello.model.entity.Seat;

/**
 * 座位服务接口
 * 定义座位相关的业务逻辑操作
 */
public interface SeatService {
    
    /**
     * 将座位实体对象转换为DTO对象
     * 
     * @param seat 座位实体对象
     * @return 座位DTO对象
     */
    SeatDTO convertToDTO(Seat seat);
    
    /**
     * 获取自习室的所有座位
     * 
     * @param studyRoomId 自习室ID
     * @return 座位列表的ResponseEntity对象
     */
    ResponseEntity<?> getSeatsByStudyRoom(String studyRoomId);
    
    /**
     * 创建新座位
     * 
     * @param seat 座位实体对象
     * @return 创建结果的ResponseEntity对象
     */
    ResponseEntity<?> createSeat(Seat seat);
    
    /**
     * 批量创建座位
     * 
     * @param studyRoomId 自习室ID
     * @param request 包含座位数量等信息的请求参数
     * @return 创建结果的ResponseEntity对象
     */
    ResponseEntity<?> createSeatsInBatch(String studyRoomId, Map<String, Object> request);
    
    /**
     * 获取座位详情
     * 
     * @param id 座位ID
     * @return 座位信息的ResponseEntity对象
     */
    ResponseEntity<?> getSeat(String id);
    
    /**
     * 更新座位状态
     * 
     * @param id 座位ID
     * @param status 包含状态信息的Map
     * @return 更新结果的ResponseEntity对象
     */
    ResponseEntity<?> updateSeatStatus(String id, Map<String, String> status);
    
    /**
     * 删除座位
     * 
     * @param id 座位ID
     * @return 删除结果的ResponseEntity对象
     */
    ResponseEntity<?> deleteSeat(String id);
    
    /**
     * 删除自习室的所有座位
     * 
     * @param studyRoomId 自习室ID
     * @return 删除结果的ResponseEntity对象
     */
    ResponseEntity<?> deleteAllSeatsByStudyRoom(String studyRoomId);
    
    /**
     * 获取座位在指定日期的实时状态
     * 
     * @param seatId 座位ID
     * @param date 日期
     * @return 座位状态信息
     */
    ResponseEntity<?> getSeatRealTimeStatus(String seatId, LocalDate date);
    
    /**
     * 获取座位在指定时间段的状态
     * 
     * @param seatId 座位ID
     * @param date 日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 座位状态信息
     */
    ResponseEntity<?> getSeatStatusForTimeSlot(String seatId, LocalDate date, LocalTime startTime, LocalTime endTime);
} 