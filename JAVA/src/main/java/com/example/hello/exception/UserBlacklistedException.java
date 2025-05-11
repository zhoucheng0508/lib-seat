package com.example.hello.exception;

public class UserBlacklistedException extends RuntimeException {
    private final long remainingTime;
    
    public UserBlacklistedException(long remainingTime) {
        super("用户处于黑名单中");
        this.remainingTime = remainingTime;
    }
    
    public long getRemainingTime() {
        return remainingTime;
    }
} 