package com.campus.dto.resp;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class OrderResp {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCover;
    private BigDecimal price;
    private String status;
    private String remark;
    private boolean buyerReviewed;
    private boolean sellerReviewed;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime finishedAt;
    private LocalDateTime cancelAt;
}
