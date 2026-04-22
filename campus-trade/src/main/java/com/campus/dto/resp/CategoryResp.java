package com.campus.dto.resp;
import lombok.Data;
import java.util.List;
@Data
public class CategoryResp {
    private Long id;
    private String name;
    private Long parentId;
    private String icon;
    private Integer sort;
    private List<CategoryResp> children;
}
