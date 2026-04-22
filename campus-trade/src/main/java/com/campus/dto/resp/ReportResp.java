package com.campus.dto.resp;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ReportResp {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long goodsId;
    private String goodsTitle;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
