package com.campus.dto.req;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
public class GoodsReq {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100字")
    private String title;

    @Size(max = 2000, message = "描述最多2000字")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "99999999.99", message = "价格超出范围")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /** ON_SALE 或 DRAFT */
    private String status = "DRAFT";

    @Size(max = 100, message = "交接地点最多100字")
    private String location;

    /** 图片URL列表，最多9张，前端先上传图片获取URL后再传 */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> imageUrls;
}
