package com.campus.dto.resp;
import lombok.Data;
import java.util.List;
@Data
public class PageResp<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;
    private int pages;
    public static <T> PageResp<T> of(List<T> records, long total, int page, int size) {
        PageResp<T> r = new PageResp<>();
        r.list = records; r.total = total;
        r.page = page; r.size = size;
        r.pages = (int) Math.ceil((double) total / size);
        return r;
    }
}
