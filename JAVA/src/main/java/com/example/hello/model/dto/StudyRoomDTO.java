package com.example.hello.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class StudyRoomDTO {
    private String id;
    private String name;
    private String location;
    private Integer capacity;
    private String description;
    private String status;
    private Long createdAt;
    private String openTime;
    private String closeTime;
    private Integer maxAdvanceDays;
    private String imageUrl;
    private List<SeatDTO> seats;
    
    @Data
    public static class TimeSlot {
        private String startTime;
        private String endTime;
        private boolean available;
    }
} 