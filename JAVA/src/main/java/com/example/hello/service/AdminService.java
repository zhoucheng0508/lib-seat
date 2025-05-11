package com.example.hello.service;

import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;

import com.example.hello.model.dto.AdminDTO;
import com.example.hello.model.dto.AdminRegisterRequest;
import com.example.hello.model.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 管理员注册
     * @param request 包含用户名、密码和验证码的注册请求
     * @return 注册结果
     */
    ResponseEntity<?> register(AdminRegisterRequest request);
    
    /**
     * 管理员登录
     * @param loginRequest 包含用户名和密码的登录请求
     * @return 登录结果
     */
    ResponseEntity<?> login(Map<String, String> loginRequest);
    
    /**
     * 根据ID获取管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    ResponseEntity<?> getAdminById(String id);
    
    /**
     * 将Admin实体转换为AdminDTO
     * @param admin 管理员实体
     * @return 管理员DTO
     */
    AdminDTO convertToDTO(Admin admin);

    /**
     * 管理员修改自身密码
     * @param changePasswordRequest 包含旧密码和新密码的请求
     * @return 修改结果
     */
    @CacheEvict(value = "admins", key = "#changePasswordRequest.get('adminId')")
    ResponseEntity<?> changePassword(Map<String, String> changePasswordRequest);
} 