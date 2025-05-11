package com.example.hello.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.hello.model.entity.StudyRoom;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.repository.StudyRoomRepository;
import com.example.hello.service.StudyRoomService;

/**
 * 自习室控制器
 * 处理自习室相关的HTTP请求
 */
@RestController
@RequestMapping("/api/admins/study-rooms")
public class StudyRoomController {
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private StudyRoomRepository studyRoomRepository;
    
    /**
     * 获取所有自习室
     */
    @GetMapping
    public ResponseEntity<?> getAllStudyRooms() {
        return studyRoomService.getAllStudyRooms();
    }
    
    /**
     * 创建自习室（JSON格式）
     */
    @PostMapping
    public ResponseEntity<?> createStudyRoom(@RequestBody StudyRoom room) {
        return studyRoomService.createStudyRoom(room);
    }
    
    /**
     * 获取指定自习室
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyRoom(@PathVariable String id) {
        return studyRoomService.getStudyRoom(id);
    }
    
    /**
     * 更新自习室（JSON格式）
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudyRoom(@PathVariable String id, @RequestBody StudyRoom room) {
        return studyRoomService.updateStudyRoom(id, room);
    }
    
    /**
     * 更新自习室状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStudyRoomStatus(@PathVariable String id, @RequestBody Map<String, String> status) {
        return studyRoomService.updateStudyRoomStatus(id, status);
    }
    
    /**
     * 专门用于上传自习室图片的接口
     * 返回图片URL，前端需要将此URL保存到自习室对象中
     */
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadStudyRoomImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "请提供有效的图片文件"));
            }
            
            // 验证自习室是否存在
            if (!studyRoomRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            // 上传图片并获取URL
            String imageUrl = uploadImageAndGetUrl(imageFile);
            
            // 更新自习室的图片URL
            return studyRoomRepository.findById(id)
                .map(studyRoom -> {
                    studyRoom.setImageUrl(imageUrl);
                    StudyRoom updatedRoom = studyRoomRepository.save(studyRoom);
                    return ResponseEntity.ok(Map.of(
                        "message", "图片上传成功",
                        "imageUrl", imageUrl,
                        "studyRoom", studyRoomService.convertToDTO(updatedRoom)
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "图片上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除自习室
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyRoom(@PathVariable String id) {
        return studyRoomService.deleteStudyRoom(id);
    }

    /**
     * 获取自习室所有座位在指定日期的实时状态
     */
    @GetMapping("/{id}/seats/real-time-status")
    public ResponseEntity<?> getStudyRoomSeatsRealTimeStatus(
            @PathVariable String id,
            @RequestParam(required = false) String dateStr) {
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        return studyRoomService.getStudyRoomSeatsRealTimeStatus(id, date);
    }
    
    /**
     * 获取自习室所有座位在指定时间段的状态
     */
    @GetMapping("/{id}/seats/status-for-time-slot")
    public ResponseEntity<?> getStudyRoomSeatsStatusForTimeSlot(
            @PathVariable String id,
            @RequestParam(required = false) String dateStr,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        return studyRoomService.getStudyRoomSeatsStatusForTimeSlot(id, date, start, end);
    }
    
    /**
     * 获取自习室在指定日期的可用时间段
     */
    @GetMapping("/{studyRoomId}/available-slots")
    public ResponseEntity<?> getAvailableTimeSlots(
            @PathVariable String studyRoomId,
            @RequestParam(required = false) String dateStr) {
        try {
            LocalDate date = dateStr != null ? 
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE) : 
                LocalDate.now();
            return studyRoomService.getAvailableTimeSlots(studyRoomId, date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "日期格式无效，请使用yyyy-MM-dd格式"));
        }
    }
    
    /**
     * 处理图片上传，并返回可访问的URL
     * 
     * @param imageFile 上传的图片文件
     * @return 图片的访问URL
     * @throws IOException 如果文件操作出错
     */
    private String uploadImageAndGetUrl(MultipartFile imageFile) throws IOException {
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        
        // 确定保存路径
        String uploadDir = "uploads/study-rooms/";
        Path uploadPath = Paths.get(uploadDir);
        
        // 确保目录存在
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回可访问的URL
        return "/uploads/study-rooms/" + fileName;
    }

    @PostMapping("/clean-orphaned-seats")
    public ResponseEntity<?> cleanOrphanedSeats() {
        return studyRoomService.cleanOrphanedSeats();
    }
} 