package com.example.hello.service;

import org.springframework.http.ResponseEntity;

import com.example.hello.entity.Feedback;

public interface FeedbackService {
    /**
     * 提交反馈
     * @param feedback 反馈信息
     * @return 提交结果
     */
    ResponseEntity<?> submitFeedback(Feedback feedback);

    /**
     * 获取用户反馈列表
     * @param userId 用户ID
     * @return 反馈列表
     */
    ResponseEntity<?> getUserFeedbacks(String userId);

    /**
     * 获取反馈详情
     * @param id 反馈ID
     * @return 反馈详情
     */
    ResponseEntity<?> getFeedbackDetail(Long id);

    /**
     * 获取所有反馈列表
     * @return 反馈列表
     */
    ResponseEntity<?> getAllFeedbacks();

    /**
     * 处理反馈
     * @param id 反馈ID
     * @param processorId 处理人ID
     * @param response 处理回复
     * @return 处理结果
     */
    ResponseEntity<?> processFeedback(Long id, String processorId, String response);
} 