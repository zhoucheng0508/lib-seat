package com.example.hello.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.hello.model.entity.Seat;
import com.example.hello.repository.SeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 座位状态缓存服务
 * 用于缓存和管理座位状态信息
 */
@Service
public class SeatStatusCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SeatStatusCacheService.class);
    private static final String SEAT_STATUS_KEY_PREFIX = "seat:status:";
    private static final String STUDY_ROOM_SEATS_KEY_PREFIX = "study_room:seats:";
    private static final long CACHE_EXPIRE_HOURS = 24; // 缓存过期时间（小时）

    private final RedisTemplate<String, Object> redisTemplate;
    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SeatStatusCacheService(RedisTemplate<String, Object> redisTemplate,
                                SeatRepository seatRepository,
                                ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.seatRepository = seatRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取座位状态缓存键
     */
    private String getSeatStatusKey(String seatId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return SEAT_STATUS_KEY_PREFIX + seatId + ":" + date + ":" + startTime + ":" + endTime;
    }

    /**
     * 获取自习室座位列表缓存键
     */
    private String getStudyRoomSeatsKey(String studyRoomId) {
        return STUDY_ROOM_SEATS_KEY_PREFIX + studyRoomId;
    }

    /**
     * 缓存座位状态
     */
    public void cacheSeatStatus(String seatId, LocalDate date, LocalTime startTime, LocalTime endTime, Map<String, Object> status) {
        if (seatId == null || date == null || startTime == null || endTime == null || status == null) {
            logger.warn("缓存座位状态失败: 参数为空");
            return;
        }
        try {
            String key = getSeatStatusKey(seatId, date, startTime, endTime);
            redisTemplate.opsForValue().set(key, status, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            logger.debug("缓存座位状态成功: key={}", key);
        } catch (Exception e) {
            logger.error("缓存座位状态失败: seatId={}, date={}, startTime={}, endTime={}", 
                seatId, date, startTime, endTime, e);
        }
    }

    /**
     * 获取缓存的座位状态
     */
    public Map<String, Object> getCachedSeatStatus(String seatId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (seatId == null || date == null || startTime == null || endTime == null) {
            logger.warn("获取缓存座位状态失败: 参数为空");
            return null;
        }
        try {
            String key = getSeatStatusKey(seatId, date, startTime, endTime);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) redisTemplate.opsForValue().get(key);
            if (result != null) {
                logger.debug("从缓存获取座位状态成功: key={}", key);
            }
            return result;
        } catch (Exception e) {
            logger.error("获取缓存座位状态失败: seatId={}, date={}, startTime={}, endTime={}", 
                seatId, date, startTime, endTime, e);
            return null;
        }
    }

    /**
     * 缓存自习室座位列表
     */
    public void cacheStudyRoomSeats(String studyRoomId, List<Seat> seats) {
        if (studyRoomId == null || seats == null) {
            logger.warn("缓存自习室座位列表失败: 参数为空");
            return;
        }
        try {
            String key = getStudyRoomSeatsKey(studyRoomId);
            redisTemplate.opsForValue().set(key, seats, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            logger.debug("缓存自习室座位列表成功: key={}, seats={}", key, seats.size());
        } catch (Exception e) {
            logger.error("缓存自习室座位列表失败: studyRoomId={}", studyRoomId, e);
        }
    }

    /**
     * 获取缓存的自习室座位列表
     */
    public List<Seat> getCachedStudyRoomSeats(String studyRoomId) {
        if (studyRoomId == null) {
            logger.warn("获取缓存自习室座位列表失败: 参数为空");
            return null;
        }
        try {
            String key = getStudyRoomSeatsKey(studyRoomId);
            @SuppressWarnings("unchecked")
            List<Seat> result = (List<Seat>) redisTemplate.opsForValue().get(key);
            if (result != null) {
                logger.debug("从缓存获取自习室座位列表成功: key={}, seats={}", key, result.size());
            }
            return result;
        } catch (Exception e) {
            logger.error("获取缓存自习室座位列表失败: studyRoomId={}", studyRoomId, e);
            return null;
        }
    }

    /**
     * 当座位状态发生变化时，清除相关缓存
     */
    public void invalidateSeatStatus(String seatId) {
        if (seatId == null) {
            logger.warn("清除座位状态缓存失败: 参数为空");
            return;
        }
        try {
            String pattern = SEAT_STATUS_KEY_PREFIX + seatId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("清除座位状态缓存成功: seatId={}, keys={}", seatId, keys.size());
            }
        } catch (Exception e) {
            logger.error("清除座位状态缓存失败: seatId={}", seatId, e);
        }
    }

    /**
     * 当自习室座位发生变化时，清除相关缓存
     */
    public void invalidateStudyRoomSeats(String studyRoomId) {
        if (studyRoomId == null) {
            logger.warn("清除自习室座位列表缓存失败: 参数为空");
            return;
        }
        try {
            String key = getStudyRoomSeatsKey(studyRoomId);
            redisTemplate.delete(key);
            logger.debug("清除自习室座位列表缓存成功: key={}", key);
        } catch (Exception e) {
            logger.error("清除自习室座位列表缓存失败: studyRoomId={}", studyRoomId, e);
        }
    }
} 