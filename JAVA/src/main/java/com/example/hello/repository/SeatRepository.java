package com.example.hello.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.model.entity.Seat;

/**
 * 座位数据访问接口
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {
    
    /**
     * 根据自习室ID查询座位列表
     * 
     * @param studyRoomId 自习室ID
     * @return 座位列表
     */
    @NonNull
    List<Seat> findByStudyRoomId(@NonNull String studyRoomId);
    
    /**
     * 根据自习室ID和座位号查询座位
     * 
     * @param studyRoomId 自习室ID
     * @param seatNumber 座位号
     * @return 座位对象
     */
    Seat findByStudyRoomIdAndSeatNumber(@NonNull String studyRoomId, @NonNull String seatNumber);
    
    /**
     * 根据自习室ID和座位号查询座位
     * 
     * @param studyRoomId 自习室ID
     * @param seatNumber 座位号
     * @return 座位对象
     */
    List<Seat> findByStudyRoomIdAndSeatNumberGreaterThan(@NonNull String studyRoomId, @NonNull String seatNumber);
    
    /**
     * 根据自习室ID和状态查询座位列表
     * 
     * @param studyRoomId 自习室ID
     * @param status 座位状态
     * @return 座位列表
     */
    @NonNull
    List<Seat> findByStudyRoomIdAndStatus(@NonNull String studyRoomId, @NonNull String status);
    
    /**
     * 统计自习室中特定状态的座位数量
     * 
     * @param studyRoomId 自习室ID
     * @param status 座位状态
     * @return 座位数量
     */
    long countByStudyRoomIdAndStatus(@NonNull String studyRoomId, @NonNull String status);
    
    /**
     * 更新座位状态
     * 
     * @param id 座位ID
     * @param status 新状态
     * @return 受影响的行数
     */
    @Modifying
    @Transactional
    @Query("UPDATE Seat s SET s.status = :status WHERE s.id = :id")
    int updateSeatStatus(@Param("id") @NonNull String id, @Param("status") @NonNull String status);
    
    /**
     * 批量更新自习室所有座位的状态
     * 
     * @param studyRoomId 自习室ID
     * @param status 新状态
     * @return 受影响的行数
     */
    @Modifying
    @Transactional
    @Query("UPDATE Seat s SET s.status = :status WHERE s.studyRoomId = :studyRoomId")
    int updateAllSeatStatusByStudyRoom(@Param("studyRoomId") @NonNull String studyRoomId, @Param("status") @NonNull String status);

    /**
     * 查找所有空闲的座位
     * 
     * @return 空闲座位列表
     */
    @Query("SELECT s FROM Seat s WHERE s.status = '空闲'")
    List<Seat> findAvailableSeats();
} 