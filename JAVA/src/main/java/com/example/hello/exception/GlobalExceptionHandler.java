package com.example.hello.exception;

import com.example.hello.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // ... 现有异常处理 ...
    
    @ExceptionHandler(CheckInTimeException.class)
    public ResponseEntity<ApiResponse<?>> handleCheckInTimeException(CheckInTimeException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(UserBlacklistedException.class)
    public ResponseEntity<ApiResponse<?>> handleUserBlacklistedException(UserBlacklistedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, ex.getMessage(), 
                        Map.of("remainingTime", ex.getRemainingTime())));
    }
} 