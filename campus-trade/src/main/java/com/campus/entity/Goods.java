package com.campus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "goods")
// 【核心修复 1】：覆写默认的 delete 行为，必须带上 AND version = ? 消耗掉乐观锁参数
@SQLDelete(sql = "UPDATE goods SET is_deleted = true WHERE id = ? AND version = ?")
// 【核心修复 2】：全局过滤，使用 JPA 的查询（如 findById, findAll）时自动排除已删除数据
@SQLRestriction("is_deleted = false")
public class Goods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** DRAFT / ON_SALE / RESERVED / SOLD */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(nullable = false)
    private Integer views = 0;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(length = 100)
    private String location;

    // 【核心修复 3】：数据库中对应的逻辑删除字段映射
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}