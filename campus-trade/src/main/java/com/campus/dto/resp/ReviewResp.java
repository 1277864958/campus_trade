package com.campus.dto.resp;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ReviewResp {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Long revieweeId;
    private Integer score;
    private String content;
    private LocalDateTime createdAt;
}
