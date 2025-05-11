package com.example.hello.service.impl;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.exception.ResourceNotFoundException;
import com.example.hello.model.dto.AdminDTO;
import com.example.hello.model.dto.AdminRegisterRequest;
import com.example.hello.model.dto.LoginResponse;
import com.example.hello.model.entity.Admin;
import com.example.hello.repository.AdminRepository;
import com.example.hello.service.AdminService;
import com.example.hello.util.JwtUtil;

/**
 * AdminService接口的实现类
 */
@Service
public class AdminServiceImpl implements AdminService {
    
    private static final String VERIFICATION_CODE = "libseat";
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public AdminDTO convertToDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setCreatedAt(admin.getCreatedAt());
        return dto;
    }
    
    @Override
    public ResponseEntity<?> register(AdminRegisterRequest request) {
        try {
            // 验证验证码
            if (!VERIFICATION_CODE.equals(request.getVerificationCode())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "验证码错误"));
            }
            
            if (adminRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "管理员用户名已存在"));
            }
            
            Admin admin = new Admin();
            admin.setUsername(request.getUsername());
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
            
            Admin savedAdmin = adminRepository.save(admin);
            return ResponseEntity.ok(convertToDTO(savedAdmin));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "注册失败: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> login(Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            System.out.println("管理员登录请求 - 用户名: " + username);
            
            Optional<Admin> adminOptional = adminRepository.findByUsername(username);
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();
                System.out.println("找到管理员 - ID: " + admin.getId());
                System.out.println("密码验证: " + passwordEncoder.matches(password, admin.getPassword()));
                
                if (passwordEncoder.matches(password, admin.getPassword())) {
                    // 管理员登录，isAdmin设置为true
                    String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), true);
                    
                    LoginResponse response = new LoginResponse();
                    response.setUserId(admin.getId());
                    response.setUsername(admin.getUsername());
                    response.setCreatedAt(admin.getCreatedAt());
                    response.setToken(token);
                    
                    System.out.println("管理员登录成功 - 用户名: " + username);
                    System.out.println("生成的token: " + token);
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            System.out.println("管理员登录失败 - 用户名或密码错误");
            return ResponseEntity.badRequest()
                .body(Map.of("message", "用户名或密码错误"));
        } catch (Exception e) {
            System.err.println("管理员登录异常: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "登录失败: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> getAdminById(String id) {
        try {
            return adminRepository.findById(id)
                    .map(admin -> ResponseEntity.ok(convertToDTO(admin)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取管理员信息失败: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> changePassword(Map<String, String> changePasswordRequest) {
        try {
            String adminId = changePasswordRequest.get("adminId");
            String oldPassword = changePasswordRequest.get("oldPassword");
            String newPassword = changePasswordRequest.get("newPassword");
            
            if (adminId == null || oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "参数不完整"));
            }
            
            Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("管理员不存在"));
            
            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "旧密码错误"));
            }
            
            // 验证新密码格式
            if (newPassword.length() < 8 || newPassword.length() > 20) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "新密码长度必须在8-20位之间"));
            }
            
            // 更新密码
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
            
            return ResponseEntity.ok(Map.of(
                "message", "密码修改成功"
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "管理员不存在"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "修改密码失败: " + e.getMessage()));
        }
    }
} 