package com.example.hello.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.example.hello.model.dto.StudyRoomDTO;
import com.example.hello.model.entity.StudyRoom;

/**
 * 自习室服务接口
 * 定义自习室相关的业务逻辑
 */
public interface StudyRoomService {
    
    /**
     * 获取所有自习室
     * 
     * @return 自习室列表
     */
    ResponseEntity<?> getAllStudyRooms();
    
    /**
     * 创建新自习室
     * 
     * @param room 自习室实体对象
     * @return 创建结果
     */
    ResponseEntity<?> createStudyRoom(StudyRoom room);
    
    /**
     * 获取自习室详情
     * 
     * @param id 自习室ID
     * @return 自习室信息
     */
    ResponseEntity<?> getStudyRoom(String id);
    
    /**
     * 更新自习室信息
     * 
     * @param id 自习室ID
     * @param room 更新后的自习室信息
     * @return 更新结果
     */
    ResponseEntity<?> updateStudyRoom(String id, StudyRoom room);
    
    /**
     * 更新自习室状态
     * 
     * @param id 自习室ID
     * @param status 包含状态信息的Map
     * @return 更新结果
     */
    ResponseEntity<?> updateStudyRoomStatus(String id, Map<String, String> status);
    
    /**
     * 删除自习室
     * 
     * @param id 自习室ID
     * @return 删除结果
     */
    ResponseEntity<?> deleteStudyRoom(String id);
    
    /**
     * 获取自习室所有座位在指定日期的实时状态
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @return 自习室所有座位的状态信息
     */
    ResponseEntity<?> getStudyRoomSeatsRealTimeStatus(String studyRoomId, LocalDate date);
    
    /**
     * 获取自习室所有座位在指定时间段的状态
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 自习室所有座位在指定时间段的状态信息
     */
    ResponseEntity<?> getStudyRoomSeatsStatusForTimeSlot(String studyRoomId, LocalDate date, LocalTime startTime, LocalTime endTime);
    
    /**
     * 获取自习室在指定日期的可用时间段
     * 
     * @param studyRoomId 自习室ID
     * @param date 日期
     * @return 可用时间段列表
     */
    ResponseEntity<?> getAvailableTimeSlots(String studyRoomId, LocalDate date);
    
    /**
     * 清理孤立的座位
     * 
     * @return 清理结果
     */
    ResponseEntity<?> cleanOrphanedSeats();
    
    StudyRoomDTO convertToDTO(StudyRoom room);
}