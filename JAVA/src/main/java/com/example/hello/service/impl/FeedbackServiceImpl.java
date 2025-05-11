package com.example.hello.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.hello.entity.Feedback;
import com.example.hello.model.entity.Admin;
import com.example.hello.repository.AdminRepository;
import com.example.hello.repository.FeedbackRepository;
import com.example.hello.repository.UserRepository;
import com.example.hello.service.FeedbackService;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public ResponseEntity<?> submitFeedback(Feedback feedback) {
        try {
            if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "反馈内容不能为空"));
            }
            if (feedback.getType() == null || feedback.getType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "反馈类型不能为空"));
            }
            feedback.setStatus("待处理");
            feedback.setCreatedAt(LocalDateTime.now());
            Feedback savedFeedback = feedbackRepository.save(feedback);
            return ResponseEntity.ok(savedFeedback);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "提交反馈失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getUserFeedbacks(String userId) {
        try {
            List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取反馈列表失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getFeedbackDetail(Long id) {
        try {
            return feedbackRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取反馈详情失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getAllFeedbacks() {
        try {
            List<Feedback> feedbacks = feedbackRepository.findAll();
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取反馈列表失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> processFeedback(Long id, String processorId, String response) {
        try {
            return feedbackRepository.findById(id)
                .map(feedback -> {
                    Admin processor = adminRepository.findById(processorId)
                        .orElseThrow(() -> new RuntimeException("管理员不存在"));
                    
                    feedback.setStatus("已处理");
                    feedback.setProcessor(processor);
                    feedback.setResponse(response);
                    feedback.setProcessedAt(LocalDateTime.now());
                    feedback.setUpdatedAt(LocalDateTime.now());
                    
                    return ResponseEntity.ok(feedbackRepository.save(feedback));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "处理反馈失败: " + e.getMessage()));
        }
    }
} 