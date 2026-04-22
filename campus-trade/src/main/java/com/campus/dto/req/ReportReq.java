package com.campus.dto.req;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ReportReq {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 200, message = "举报原因最多200字")
    private String reason;
}
