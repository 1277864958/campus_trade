package com.campus.entity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@NoArgsConstructor
@Entity @Table(name = "chat_sessions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"buyer_id","seller_id","goods_id"}))
public class ChatSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getGoodsId() { return goodsId; }
    public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
