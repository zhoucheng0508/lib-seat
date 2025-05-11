package com.example.hello.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "admins")
@Getter
@Setter
public class Admin {
    @Id
    @Column(length = 32)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 19);
            this.id = timestamp + uuid;
        }
        if (createdAt == null) {
            this.createdAt = System.currentTimeMillis();
        }
    }
} 