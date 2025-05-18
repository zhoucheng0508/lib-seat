package com.example.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;  // 添加这个导入

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@SpringBootApplication
@EnableScheduling 
public class HelloApplication {
    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);
    public static void main(String[] args) {
        
        SpringApplication.run(HelloApplication.class, args);
    }

}
