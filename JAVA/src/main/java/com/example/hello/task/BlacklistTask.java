package com.example.hello.task;

import com.example.hello.model.entity.User;
import com.example.hello.repository.UserRepository;
import com.example.hello.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BlacklistTask {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void checkBlacklist() {
        List<User> blacklistedUsers = userRepository.findByIsBlacklistedTrue();
        LocalDateTime now = LocalDateTime.now();
        
        for (User user : blacklistedUsers) {
            if (user.getBlacklistStartTime() != null) {
                LocalDateTime releaseTime = user.getBlacklistStartTime().plusDays(2);
                if (now.isAfter(releaseTime)) {
                    userService.removeFromBlacklist(user.getId());
                }
            }
        }
    }
} 