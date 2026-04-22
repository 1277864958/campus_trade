package com.campus.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data @NoArgsConstructor
@Entity @Table(name = "goods_images")
public class GoodsImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;
    @Column(name = "image_url", nullable = false, length = 300)
    private String imageUrl;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
