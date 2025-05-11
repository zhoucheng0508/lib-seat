package com.example.hello.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hello.model.dto.AdminRegisterRequest;
import com.example.hello.service.AdminService;
import com.example.hello.service.UserService;

/**
 * 管理员控制器
 * 处理管理员相关的HTTP请求
 */
@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AdminRegisterRequest request) {
        return adminService.register(request);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        System.out.println("=== Admin Login Request ===");
        System.out.println("Username: " + loginRequest.get("username"));
        System.out.println("Password length: " + (loginRequest.get("password") != null ? loginRequest.get("password").length() : 0));
        return adminService.login(loginRequest);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable String id) {
        return adminService.getAdminById(id);
    }
    
    /**
     * 管理员修改自身密码
     * @param changePasswordRequest 包含旧密码和新密码的请求
     * @return 修改结果
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> changePasswordRequest) {
        return adminService.changePassword(changePasswordRequest);
    }
    
    /**
     * 管理员修改用户密码
     * @param changePasswordRequest 包含用户ID和新密码的请求
     * @return 修改结果
     */
    @PutMapping("/users/change-password")
    public ResponseEntity<?> adminChangeUserPassword(@RequestBody Map<String, String> changePasswordRequest) {
        return userService.adminChangeUserPassword(changePasswordRequest);
    }

    /**
     * 管理员获取所有用户信息
     * @return 所有用户信息列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return userService.getAllUsers();
    }
} 