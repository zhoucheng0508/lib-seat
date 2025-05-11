package com.example.hello.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    // 使用固定的密钥
    private static final String SECRET_KEY = "libseat-jwt-secret-key-must-be-at-least-32-characters";
    private static final Key key = new SecretKeySpec(
        SECRET_KEY.getBytes(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.getJcaName()
    );
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    public String generateToken(String userId, String username, boolean isAdmin) {
        System.out.println("生成token - userId: " + userId);
        System.out.println("生成token - username: " + username);
        System.out.println("生成token - isAdmin: " + isAdmin);
        
        String role = isAdmin ? "ADMIN" : "USER";
        System.out.println("生成token - role: " + role);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public void validateToken(String token) {
        try {
            System.out.println("验证token: " + token);
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            System.out.println("Token验证成功");
            System.out.println("Token信息 - subject: " + claims.getSubject());
            System.out.println("Token信息 - role: " + claims.get("role"));
            System.out.println("Token信息 - userId: " + claims.get("userId"));
            System.out.println("Token信息 - expiration: " + claims.getExpiration());
        } catch (Exception e) {
            System.err.println("Token验证失败: " + e.getMessage());
            throw e;
        }
    }
    
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
} 