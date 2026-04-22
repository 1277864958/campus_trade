package com.campus.dto.resp;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class AdminStatResp {
    private long totalUsers;
    private long totalGoods;
    private long totalOrders;
    private long finishedOrders;
    private BigDecimal totalAmount;
    private long pendingReports;
}
