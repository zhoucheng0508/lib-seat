package com.example.hello.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hello.exception.ResourceNotFoundException;
import com.example.hello.model.dto.LoginResponse;
import com.example.hello.model.dto.UserDTO;
import com.example.hello.model.entity.User;
import com.example.hello.repository.UserRepository;
import com.example.hello.service.UserService;
import com.example.hello.util.JwtUtil;

/**
 * UserService接口的实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    
    @Override
    public ResponseEntity<?> createUser(User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户信息不能为空"));
            }
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(System.currentTimeMillis());
            user.setIsBlacklisted(false);
            user.setNoShowCount(0);
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(convertToDTO(savedUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "创建用户失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getUserById(String id) {
        try {
            return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取用户信息失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> updateUser(String id, User user) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户ID不能为空"));
            }
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户信息不能为空"));
            }
            
            return userRepository.findById(id)
                .map(existingUser -> {
                    if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                        if (!existingUser.getUsername().equals(user.getUsername()) && 
                            userRepository.existsByUsername(user.getUsername())) {
                            return ResponseEntity.badRequest()
                                .body(Map.of("message", "用户名已存在"));
                        }
                        existingUser.setUsername(user.getUsername());
                    }
                    
                    if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    
                    return ResponseEntity.ok(convertToDTO(userRepository.save(existingUser)));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "更新用户信息失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> deleteUser(String id) {
        try {
            return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(Map.of("message", "用户删除成功"));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "删除用户失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "获取用户列表失败: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> login(Map<String, String> loginRequest) {
        try {
            if (loginRequest == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "登录请求不能为空"));
            }
            
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "用户名和密码不能为空"));
            }
            
            User user = userRepository.findByUsername(username);
            if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "用户名或密码错误"));
            }
            
            // 生成token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), false);
            
            // 创建登录响应对象
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setCreatedAt(user.getCreatedAt());
            response.setToken(token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "登录失败: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> register(User user) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "用户名已存在"));
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 保存用户
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(convertToDTO(savedUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "注册失败: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> register(Map<String, String> registerRequest) {
        try {
            if (registerRequest == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "注册请求不能为空"));
            }
            
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "用户名和密码不能为空"));
            }
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "用户名已存在"));
            }
            
            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            
            // 保存用户
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                "message", "注册成功",
                "user", convertToDTO(savedUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "注册失败: " + e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getUserBlacklistStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        Map<String, Object> status = new HashMap<>();
        
        // 如果为null，说明没有未签到记录
        status.put("noShowCount", user.getNoShowCount() != null ? user.getNoShowCount() : 0);
        
        // 如果为null，说明不在黑名单中
        status.put("isBlacklisted", Boolean.TRUE.equals(user.getIsBlacklisted()));
        
        // 如果在黑名单中且有开始时间，计算剩余时间
        if (Boolean.TRUE.equals(user.getIsBlacklisted()) && user.getBlacklistStartTime() != null) {
            long remainingTime = ChronoUnit.MILLIS.between(
                LocalDateTime.now(),
                user.getBlacklistStartTime().plusDays(2)
            );
            status.put("remainingTime", Math.max(0, remainingTime));
        }
        
        return status;
    }
    
    @Override
    @Transactional
    public void incrementNoShowCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 如果为null，说明是第一次未签到
        int currentCount = user.getNoShowCount() != null ? user.getNoShowCount() : 0;
        user.setNoShowCount(currentCount + 1);
        
        // 如果达到3次，加入黑名单
        if (currentCount + 1 >= 3) {
            user.setIsBlacklisted(true);
            user.setBlacklistStartTime(LocalDateTime.now());
        }
        
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void removeFromBlacklist(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setIsBlacklisted(false);
        user.setNoShowCount(null);  // 重置为null，表示清除未签到记录
        user.setBlacklistStartTime(null);
        userRepository.save(user);
    }

    @Override
    public List<User> getBlacklistedUsers() {
        return userRepository.findByIsBlacklistedTrue();
    }
    
    @Override
    public long calculateRemainingBlacklistTime(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        if (!Boolean.TRUE.equals(user.getIsBlacklisted()) || user.getBlacklistStartTime() == null) {
            return 0;
        }
        
        LocalDateTime endTime = user.getBlacklistStartTime().plusDays(2);
        long remainingTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), endTime);
        return Math.max(0, remainingTime);
    }

    @Override
    public ResponseEntity<?> getAllBlacklistedUsers() {
        List<User> blacklistedUsers = userRepository.findByIsBlacklistedTrue();
        if (blacklistedUsers.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "当前黑名单中没有成员",
                "data", Collections.emptyList()
            ));
        }
        
        List<Map<String, Object>> blacklistData = blacklistedUsers.stream()
            .map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", user.getId());
                userData.put("username", user.getUsername());
                userData.put("blacklistTime", user.getBlacklistStartTime());
                userData.put("remainingTime", calculateRemainingBlacklistTime(user.getId()));
                userData.put("reason", "多次未签到");
                return userData;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success",
            "data", blacklistData
        ));
    }
    
    @Override
    public ResponseEntity<?> adminRemoveFromBlacklist(String userId) {
        try {
            removeFromBlacklist(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "用户已从黑名单中移除");
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "移除黑名单失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @Override
    public ResponseEntity<?> adminAddToBlacklist(String userId, Integer days) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            
            // 如果已在黑名单中
            if (Boolean.TRUE.equals(user.getIsBlacklisted())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "用户已在黑名单中",
                    "data", null
                ));
            }
            
            // 设置黑名单状态
            user.setIsBlacklisted(true);
            user.setBlacklistStartTime(LocalDateTime.now());
            
            // 默认黑名单期限为2天
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "用户已添加到黑名单，期限2天",
                "data", Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "blacklistTime", user.getBlacklistStartTime()
                )
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "code", 404,
                "message", "用户不存在",
                "data", null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "code", 500,
                "message", "添加到黑名单失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> changePassword(Map<String, String> changePasswordRequest) {
        try {
            String userId = changePasswordRequest.get("userId");
            String oldPassword = changePasswordRequest.get("oldPassword");
            String newPassword = changePasswordRequest.get("newPassword");
            
            if (userId == null || oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "参数不完整"));
            }
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            
            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "旧密码错误"));
            }
            
            // 验证新密码格式
            if (newPassword.length() < 8 || newPassword.length() > 20) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "新密码长度必须在8-20位之间"));
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "密码修改成功"
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "修改密码失败: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> adminChangeUserPassword(Map<String, String> changePasswordRequest) {
        try {
            String userId = changePasswordRequest.get("userId");
            String newPassword = changePasswordRequest.get("newPassword");
            
            if (userId == null || newPassword == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "参数不完整"));
            }
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            
            // 验证新密码格式
            if (newPassword.length() < 8 || newPassword.length() > 20) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "新密码长度必须在8-20位之间"));
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "密码修改成功"
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "修改密码失败: " + e.getMessage()));
        }
    }
} 