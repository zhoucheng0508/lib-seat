package com.example.hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RedisConnectionTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionTest.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        try {
            logger.info("开始测试Redis连接...");
            
            // 测试写入
            String key = "test:key";
            String value = "test:value";
            logger.info("尝试写入数据: key={}, value={}", key, value);
            redisTemplate.opsForValue().set(key, value);
            
            // 测试读取
            logger.info("尝试读取数据: key={}", key);
            Object retrievedValue = redisTemplate.opsForValue().get(key);
            logger.info("读取到的数据: {}", retrievedValue);
            assertEquals(value, retrievedValue);
            
            // 测试删除
            logger.info("尝试删除数据: key={}", key);
            redisTemplate.delete(key);
            Object deletedValue = redisTemplate.opsForValue().get(key);
            logger.info("删除后读取数据: {}", deletedValue);
            assertNull(deletedValue);
            
            logger.info("Redis连接测试完成");
        } catch (Exception e) {
            logger.error("Redis连接测试失败", e);
            throw e;
        }
    }
} 