package com.campus.dto.req;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class GoodsSearchReq {
    private String keyword;
    private Long   categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    /** created_at / price_asc / price_desc / views */
    private String sortBy = "created_at";
    private int    page   = 0;
    private int    size   = 12;
}
