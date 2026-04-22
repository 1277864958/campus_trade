package com.campus.dto.resp;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class GoodsResp {
    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Long categoryId;
    private String categoryName;
    private String status;
    private Integer views;
    private Integer likes;
    private String location;
    private List<String> imageUrls;
    private String coverUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
