package com.campus.dto.req;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class OrderReq {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    private String remark;
}
