package com.example.hello.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.service.UserService;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        return userService.register(registerRequest);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        return userService.login(loginRequest);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }
    
    /**
     * 用户修改自己的密码
     * @param changePasswordRequest 包含旧密码和新密码的请求
     * @return 修改结果
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> changePasswordRequest) {
        return userService.changePassword(changePasswordRequest);
    }
    
    /**
     * 管理员获取黑名单用户列表
     */
    @GetMapping("/blacklist")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getBlacklist() {
        return userService.getAllBlacklistedUsers();
    }
    
    /**
     * 管理员将用户从黑名单中移除
     */
    @DeleteMapping("/blacklist/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> removeFromBlacklist(@PathVariable String userId) {
        return userService.adminRemoveFromBlacklist(userId);
    }
    
    /**
     * 管理员将用户添加到黑名单
     */
    @PostMapping("/blacklist/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addToBlacklist(@PathVariable String userId) {
        return userService.adminAddToBlacklist(userId, 2);  // 默认黑名单期限2天
    }
} 