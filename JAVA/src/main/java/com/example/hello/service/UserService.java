package com.example.hello.service;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;

import com.example.hello.model.dto.UserDTO;
import com.example.hello.model.entity.User;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 */
public interface UserService {
    
    /**
     * 用户注册（使用Map参数）
     * @param registerRequest 包含用户名和密码的注册请求
     * @return 注册结果
     */
    ResponseEntity<?> register(Map<String, String> registerRequest);
    
    /**
     * 用户注册（使用User对象）
     * @param user 用户对象
     * @return 注册结果
     */
    ResponseEntity<?> register(User user);
    
    /**
     * 用户登录
     * @param loginRequest 包含用户名和密码的登录请求
     * @return 登录结果
     */
    ResponseEntity<?> login(Map<String, String> loginRequest);
    
    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    ResponseEntity<?> getUserById(String id);

    /**
     * 创建新用户
     * @param user 用户对象
     * @return 创建结果
     */
    ResponseEntity<?> createUser(User user);

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 更新后的用户信息
     * @return 更新结果
     */
    ResponseEntity<?> updateUser(String id, User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    ResponseEntity<?> deleteUser(String id);
    
    /**
     * 将User实体转换为UserDTO
     * @param user 用户实体
     * @return 用户DTO
     */
    UserDTO convertToDTO(User user);

    /**
     * 用户修改密码
     * @param changePasswordRequest 包含旧密码和新密码的请求
     * @return 修改结果
     */
    @CacheEvict(value = "users", key = "#changePasswordRequest.get('userId')")
    ResponseEntity<?> changePassword(Map<String, String> changePasswordRequest);

    /**
     * 管理员修改用户密码
     * @param changePasswordRequest 包含用户ID和新密码的请求
     * @return 修改结果
     */
    @CacheEvict(value = "users", key = "#changePasswordRequest.get('userId')")
    ResponseEntity<?> adminChangeUserPassword(Map<String, String> changePasswordRequest);

    /**
     * 管理员获取所有用户信息
     * @return 所有用户信息列表
     */
    @Cacheable(value = "users")
    ResponseEntity<?> getAllUsers();

    Map<String, Object> getUserBlacklistStatus(String userId);
    void incrementNoShowCount(String userId);
    void removeFromBlacklist(String userId);

    /**
     * 获取所有黑名单用户信息
     * 
     * @return 黑名单用户列表的ResponseEntity对象
     */
    @Cacheable(value = "blacklist")
    ResponseEntity<?> getAllBlacklistedUsers();
    
    /**
     * 将用户从黑名单中移除（管理员操作）
     * 
     * @param userId 用户ID
     * @return 操作结果的ResponseEntity对象
     */
    @CacheEvict(value = "blacklist", allEntries = true)
    ResponseEntity<?> adminRemoveFromBlacklist(String userId);

    /**
     * 获取所有黑名单用户
     * 
     * @return 黑名单用户列表
     */
    List<User> getBlacklistedUsers();
    
    /**
     * 计算用户剩余黑名单时间
     * 
     * @param userId 用户ID
     * @return 剩余时间（毫秒）
     */
    long calculateRemainingBlacklistTime(String userId);

    /**
     * 管理员将用户添加到黑名单
     * 
     * @param userId 用户ID
     * @return 操作结果的ResponseEntity对象
     */
    @CacheEvict(value = "blacklist", allEntries = true)
    ResponseEntity<?> adminAddToBlacklist(String userId, Integer days);
} 