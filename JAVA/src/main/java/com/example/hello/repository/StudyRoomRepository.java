package com.example.hello.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hello.model.entity.StudyRoom;

/**
 * 自习室数据访问接口
 */
@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, String> {
    
    /**
     * 根据状态查询自习室列表
     * 
     * @param status 自习室状态
     * @return 自习室列表
     */
    List<StudyRoom> findByStatus(String status);
} 