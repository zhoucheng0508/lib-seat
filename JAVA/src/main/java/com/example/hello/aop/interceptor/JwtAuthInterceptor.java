package com.example.hello.aop.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.hello.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 * 用于拦截需要认证的API请求，验证JWT令牌的有效性
 */
public class JwtAuthInterceptor implements HandlerInterceptor {
    
    /**
     * 注入JWT工具类，用于令牌验证
     */
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 请求预处理方法，在Controller方法执行前被调用
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @return 如果返回true则继续处理，返回false则中断请求
     * @throws Exception 可能抛出的异常
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                            @NonNull HttpServletResponse response, 
                            @NonNull Object handler) throws Exception {
        System.out.println("\n=== JWT Auth Interceptor ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        
        // 允许OPTIONS请求通过，解决CORS预检问题
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("OPTIONS请求，允许通过");
            return true;
        }
        
        // 获取请求头中的Authorization字段
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.getClaimsFromToken(token);
                String role = (String) claims.get("role");  // 直接使用token中的role
                String uri = request.getRequestURI();
                
                System.out.println("Token验证成功");
                System.out.println("Token信息 - subject: " + claims.getSubject());
                System.out.println("Token信息 - role: " + role);
                System.out.println("Token信息 - userId: " + claims.get("userId"));
                System.out.println("Token信息 - expiration: " + claims.getExpiration());
                
                // 管理员接口需要ADMIN角色
                if (uri.startsWith("/api/admins/")) {
                    System.out.println("访问管理员接口 - 需要ADMIN角色");
                    if (!"ADMIN".equals(role)) {
                        System.out.println("访问拒绝：需要管理员权限");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                    System.out.println("管理员权限验证通过");
                }
                
                // 预约相关接口需要登录用户
                if (uri.startsWith("/api/reservations/")) {
                    System.out.println("访问预约接口 - 需要用户登录");
                    if (role == null || (!role.equals("USER") && !role.equals("ADMIN"))) {
                        System.out.println("访问拒绝：需要用户权限");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                    System.out.println("用户权限验证通过");
                }
                
                return true;
            } catch (Exception e) {
                System.err.println("Token验证失败: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        
        System.out.println("未找到有效的认证头");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
} 