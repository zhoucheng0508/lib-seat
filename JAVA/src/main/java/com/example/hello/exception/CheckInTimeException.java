package com.example.hello.exception;

public class CheckInTimeException extends RuntimeException {
    public CheckInTimeException(String message) {
        super(message);
    }
} 