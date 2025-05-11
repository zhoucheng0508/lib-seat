package com.example.hello.service;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;

import com.example.hello.model.dto.ReservationDTO;
import com.example.hello.model.entity.Reservation;

/**
 * 预约服务接口
 * 定义预约相关的业务逻辑操作
 */
public interface ReservationService {
    
    /**
     * 将预约实体对象转换为DTO对象
     * 
     * @param reservation 预约实体对象
     * @return 预约DTO对象
     */
    ReservationDTO convertToDTO(Reservation reservation);
    
    /**
     * 创建新预约
     * 
     * @param reservation 预约实体对象
     * @return 创建结果的ResponseEntity对象
     */
    @CacheEvict(value = {"reservations", "userReservations", "seatReservations", "studyRoomReservations"}, allEntries = true)
    ResponseEntity<?> createReservation(Reservation reservation);
    
    /**
     * 获取预约详情
     * 
     * @param id 预约ID
     * @return 预约信息的ResponseEntity对象
     */
    @Cacheable(value = "reservations", key = "#id")
    ResponseEntity<?> getReservation(String id);
    
    /**
     * 获取用户的预约列表
     * 
     * @param userId 用户ID
     * @return 预约列表的ResponseEntity对象
     */
    @Cacheable(value = "userReservations", key = "#userId")
    ResponseEntity<?> getUserReservations(String userId);
    
    /**
     * 获取特定状态的用户预约
     * 
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约列表的ResponseEntity对象
     */
    @Cacheable(value = "userReservations", key = "#userId + ':' + #status")
    ResponseEntity<?> getUserReservationsByStatus(String userId, String status);
    
    /**
     * 获取座位的预约列表
     * 
     * @param seatId 座位ID
     * @return 预约列表的ResponseEntity对象
     */
    @Cacheable(value = "seatReservations", key = "#seatId")
    ResponseEntity<?> getSeatReservations(String seatId);
    
    /**
     * 获取自习室的预约列表
     * 
     * @param studyRoomId 自习室ID
     * @return 预约列表的ResponseEntity对象
     */
    @Cacheable(value = "studyRoomReservations", key = "#studyRoomId")
    ResponseEntity<?> getStudyRoomReservations(String studyRoomId);
    
    /**
     * 获取特定日期的预约列表
     * 
     * @param date 预约日期
     * @return 预约列表的ResponseEntity对象
     */
    @Cacheable(value = "reservations", key = "#date.toString()")
    ResponseEntity<?> getReservationsByDate(LocalDate date);
    
    /**
     * 取消预约
     * 
     * @param id 预约ID
     * @return 取消结果的ResponseEntity对象
     */
    @CacheEvict(value = {"reservations", "userReservations", "seatReservations", "studyRoomReservations"}, allEntries = true)
    ResponseEntity<?> cancelReservation(String id);
    
    /**
     * 完成预约
     * 
     * @param id 预约ID
     * @return 完成结果的ResponseEntity对象
     */
    @CacheEvict(value = {"reservations", "userReservations", "seatReservations", "studyRoomReservations"}, allEntries = true)
    ResponseEntity<?> completeReservation(String id);
    
    /**
     * 检查座位在特定时间段是否可预约
     * 
     * @param seatId 座位ID
     * @param date 预约日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 检查结果的ResponseEntity对象
     */
    @Cacheable(value = "seatAvailability", key = "#seatId + ':' + #date.toString() + ':' + #startTime + ':' + #endTime")
    ResponseEntity<?> checkSeatAvailability(String seatId, LocalDate date, String startTime, String endTime);
    
    /**
     * 获取自习室指定日期的可用时间段
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @return 可用时间段的ResponseEntity对象
     */
    @Cacheable(value = "availableTimeSlots", key = "#studyRoomId + ':' + #date.toString()")
    ResponseEntity<?> getAvailableTimeSlots(String studyRoomId, LocalDate date);
    
    /**
     * 获取自习室在指定时间段的状态
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 自习室状态
     */
    @Cacheable(value = "studyRoomStatus", key = "#studyRoomId + ':' + #date.toString() + ':' + #startTime + ':' + #endTime")
    ResponseEntity<?> getStudyRoomStatus(String studyRoomId, LocalDate date, String startTime, String endTime);
    
    /**
     * 获取自习室在指定时间段的所有座位状态
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 座位状态列表
     */
    @Cacheable(value = "studyRoomSeatsStatus", key = "#studyRoomId + ':' + #date.toString() + ':' + #startTime + ':' + #endTime")
    ResponseEntity<?> getStudyRoomSeatsStatus(String studyRoomId, LocalDate date, String startTime, String endTime);
    
    /**
     * 获取所有自习室在指定时间段的状态
     */
    @Cacheable(value = "studyRoomsStatus", key = "#date.toString() + ':' + #startTime + ':' + #endTime")
    ResponseEntity<?> getStudyRoomsStatus(LocalDate date, String startTime, String endTime);
    
    /**
     * 获取自习室详情和座位信息
     */
    @Cacheable(value = "studyRoomDetail", key = "#studyRoomId + ':' + #date.toString() + ':' + #startTime + ':' + #endTime")
    ResponseEntity<?> getStudyRoomDetail(String studyRoomId, LocalDate date, String startTime, String endTime);
} 