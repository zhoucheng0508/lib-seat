package com.example.hello.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hello.dto.AdminReservationDTO;
import com.example.hello.model.entity.Reservation;

/**
 * 预约数据访问接口
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    
    /**
     * 根据用户ID查询预约列表
     * 
     * @param userId 用户ID
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findByUserId(@Param("userId") String userId);
    
    /**
     * 根据座位ID查询预约列表
     * 
     * @param seatId 座位ID
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findBySeatId(@Param("seatId") String seatId);
    
    /**
     * 根据自习室ID查询预约列表
     * 
     * @param studyRoomId 自习室ID
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.studyRoomId = :studyRoomId AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findByStudyRoomId(@Param("studyRoomId") String studyRoomId);
    
    /**
     * 根据用户ID和状态查询预约列表
     * 
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = :status AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    /**
     * 根据日期查询预约列表
     * 
     * @param date 预约日期
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.date = :date AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findByDate(@Param("date") LocalDate date);
    
    /**
     * 根据座位ID和日期查询预约列表
     * 
     * @param seatId 座位ID
     * @param date 预约日期
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId AND r.date = :date AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findBySeatIdAndDate(@Param("seatId") String seatId, @Param("date") LocalDate date);
    
    /**
     * 查找指定座位在指定日期和时间段内的重叠预约
     */
    @Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId " +
           "AND r.date = :date " +
           "AND r.status = 'CONFIRMED' " +
           "AND (r.isDeleted IS NULL OR r.isDeleted = false) " +
           "AND ((r.startTime <= :startTime AND r.endTime > :startTime) " +
           "OR (r.startTime < :endTime AND r.endTime >= :endTime) " +
           "OR (r.startTime >= :startTime AND r.endTime <= :endTime))")
    List<Reservation> findOverlappingReservations(
        @Param("seatId") String seatId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);
    
    /**
     * 获取用户在指定日期的预约次数（不包括已取消的预约）
     * 
     * @param userId 用户ID
     * @param date 预约日期
     * @param status 排除的状态（如'CANCELLED'）
     * @return 预约次数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.date = :date AND r.status != :status AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    long countByUserIdAndDateAndStatusNot(@Param("userId") String userId, @Param("date") LocalDate date, @Param("status") String status);

    /**
     * 获取特定自习室在特定日期的所有预约
     */
    @Query("SELECT r FROM Reservation r WHERE r.studyRoomId = :studyRoomId AND r.date = :date AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findByStudyRoomIdAndDate(@Param("studyRoomId") String studyRoomId, @Param("date") LocalDate date);

    /**
     * 查询自习室在指定时间段内的预约
     */
    @Query("SELECT r FROM Reservation r WHERE r.studyRoomId = :studyRoomId " +
           "AND r.date = :date " +
           "AND r.status != 'CANCELLED' " +
           "AND (r.isDeleted IS NULL OR r.isDeleted = false) " +
           "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<Reservation> findByStudyRoomIdAndTimeRange(
        @Param("studyRoomId") String studyRoomId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);

    boolean existsBySeatId(String seatId);

    /**
     * 检查指定座位在指定日期和时间段是否有预约
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.seatId = :seatId " +
           "AND r.date = :date " +
           "AND r.status = 'CONFIRMED' " +
           "AND ((r.startTime <= :startTime AND r.endTime > :startTime) " +
           "OR (r.startTime < :endTime AND r.endTime >= :endTime) " +
           "OR (r.startTime >= :startTime AND r.endTime <= :endTime))")
    boolean existsBySeatIdAndDateAndTimeRange(
        @Param("seatId") String seatId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);

    List<Reservation> findByDateAndEndTimeBeforeAndStatusIn(
        LocalDate date,
        LocalTime endTime,
        List<String> statuses
    );

    @Query("SELECT new com.example.hello.dto.AdminReservationDTO(" +
           "r.id, r.userId, u.username, r.seatId, s.seatNumber, " +
           "r.studyRoomId, sr.name, r.date, r.startTime, r.endTime, " +
           "r.status, r.isDeleted, r.deletedBy, r.deletedAt, " +
           "r.adjustedBy, r.adjustedAt) " +
           "FROM Reservation r " +
           "LEFT JOIN User u ON r.userId = u.id " +
           "LEFT JOIN Seat s ON r.seatId = s.id " +
           "LEFT JOIN StudyRoom sr ON r.studyRoomId = sr.id " +
           "WHERE (:userId IS NULL OR r.userId = :userId) AND " +
           "(:startDate IS NULL OR r.date >= :startDate) AND " +
           "(:endDate IS NULL OR r.date <= :endDate) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:seatId IS NULL OR r.seatId = :seatId) AND " +
           "(:studyRoomId IS NULL OR r.studyRoomId = :studyRoomId) AND " +
           "(r.isDeleted IS NULL OR r.isDeleted = false)")
    Page<AdminReservationDTO> findByConditions(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") String status,
        @Param("seatId") String seatId,
        @Param("studyRoomId") String studyRoomId,
        Pageable pageable);
        
    @Query("DELETE FROM Reservation r WHERE r.isDeleted = true AND r.deletedAt < :date")
    void deleteByIsDeletedTrueAndDeletedAtBefore(@Param("date") LocalDate date);

    /**
     * 查找用户在指定日期和时间段内的重叠预约
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId " +
           "AND r.date = :date " +
           "AND r.status = 'CONFIRMED' " +
           "AND (r.isDeleted IS NULL OR r.isDeleted = false) " +
           "AND ((r.startTime <= :startTime AND r.endTime > :startTime) " +
           "OR (r.startTime < :endTime AND r.endTime >= :endTime) " +
           "OR (r.startTime >= :startTime AND r.endTime <= :endTime))")
    List<Reservation> findUserOverlappingReservations(
        @Param("userId") String userId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);

    /**
     * 根据用户ID和日期查询预约列表
     * 
     * @param userId 用户ID
     * @param date 预约日期
     * @return 预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.date = :date AND (r.isDeleted IS NULL OR r.isDeleted = false)")
    List<Reservation> findByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE " +
       "(" +
       "   (r.date < :date) OR " +  // 日期小于今天
       "   (r.date = :date AND r.endTime <= :endTime)" +  // 日期等于今天且结束时间小于等于当前时间
       ") AND " +
       "r.status IN :statuses AND " +  // 状态匹配
       "(r.isDeleted IS NULL OR r.isDeleted = false)")  // 未删除的预约
List<Reservation> findExpiredReservations(
    @Param("date") LocalDate date,
    @Param("endTime") LocalTime endTime,
    @Param("statuses") List<String> statuses);
} 