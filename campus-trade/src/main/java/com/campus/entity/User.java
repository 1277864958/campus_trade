package com.campus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体 — users 表
 */
@Data @NoArgsConstructor
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 128)
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Column(name = "avatar_url", length = 300)
    private String avatarUrl;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
