package com.campus.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data @NoArgsConstructor
@Entity @Table(name = "reviews")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;
    @Column(name = "reviewee_id", nullable = false)
    private Long revieweeId;
    @Column(nullable = false)
    private Integer score;
    @Column(length = 500)
    private String content;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
