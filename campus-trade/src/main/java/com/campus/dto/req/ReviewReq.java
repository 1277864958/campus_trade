package com.campus.dto.req;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ReviewReq {
    @NotNull private Long orderId;
    @NotNull @Min(1) @Max(5) private Integer score;
    @Size(max = 500, message = "评价内容最多500字")
    private String content;
}
