package com.campus.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data @NoArgsConstructor
@Entity @Table(name = "reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;
    @Column(nullable = false, length = 200)
    private String reason;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
