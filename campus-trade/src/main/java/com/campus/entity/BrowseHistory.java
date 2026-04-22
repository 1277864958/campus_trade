package com.campus.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data @NoArgsConstructor
@Entity @Table(name = "browse_history")
public class BrowseHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
