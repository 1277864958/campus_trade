package com.campus.dto.req;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class ChatSessionReq {
    @NotNull private Long sellerId;
    @NotNull private Long goodsId;
}
