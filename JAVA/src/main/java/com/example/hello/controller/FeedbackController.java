package com.example.hello.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.entity.Feedback;
import com.example.hello.service.FeedbackService;

@RestController
@RequestMapping("/api")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody Feedback feedback) {
        return feedbackService.submitFeedback(feedback);
    }

    @GetMapping("/feedback")
    public ResponseEntity<?> getUserFeedbacks(@RequestParam String userId) {
        return feedbackService.getUserFeedbacks(userId);
    }

    @GetMapping("/feedback/{id}")
    public ResponseEntity<?> getFeedbackDetail(@PathVariable Long id) {
        return feedbackService.getFeedbackDetail(id);
    }

    @GetMapping("/admin/feedback")
    public ResponseEntity<?> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @PutMapping("/admin/feedback/{id}")
    public ResponseEntity<?> processFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String processorId = request.get("processorId");
        String response = request.get("response");
        if (processorId == null || processorId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
                .body(Map.of("message", "处理人ID不能为空"));
        }
        if (response == null || response.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
                .body(Map.of("message", "处理回复不能为空"));
        }
        return feedbackService.processFeedback(id, processorId, response);
    }
} 