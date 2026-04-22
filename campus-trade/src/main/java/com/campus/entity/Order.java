package com.campus.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    @Column(length = 500)
    private String remark;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    @Column(name = "cancel_at")
    private LocalDateTime cancelAt;
}
