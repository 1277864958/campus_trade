package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.dto.req.*;
import com.campus.dto.resp.*;
import com.campus.service.impl.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {
    private final GoodsService  goodsService;
    private final ReportService reportService;

    @GetMapping
    public Result<PageResp<GoodsResp>> search(GoodsSearchReq req) {
        return Result.success(goodsService.search(req));
    }
    @GetMapping("/{id}")
    public Result<GoodsResp> detail(@PathVariable Long id,
                                    @AuthenticationPrincipal Long userId) {
        return Result.success(goodsService.getDetail(id, userId));
    }
    @PostMapping
    public Result<GoodsResp> publish(@AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody GoodsReq req) {
        return Result.success("发布成功", goodsService.publish(userId, req));
    }
    @PutMapping("/{id}")
    public Result<GoodsResp> update(@AuthenticationPrincipal Long userId,
                                    @PathVariable Long id, @Valid @RequestBody GoodsReq req) {
        return Result.success(goodsService.update(userId, id, req));
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        goodsService.delete(userId, id); return Result.success("删除成功");
    }
    @PutMapping("/{id}/on-sale")
    public Result<Void> onSale(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        goodsService.putOnSale(userId, id); return Result.success("已上架");
    }
    @PutMapping("/{id}/take-down")
    public Result<Void> takeDown(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        goodsService.takeDown(userId, id); return Result.success("已下架");
    }
    @GetMapping("/my")
    public Result<PageResp<GoodsResp>> myGoods(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(goodsService.myGoods(userId, status, page, size));
    }
    @GetMapping("/history")
    public Result<PageResp<GoodsResp>> history(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(goodsService.browseHistory(userId, page, size));
    }
    @PostMapping("/report")
    public Result<Void> report(@AuthenticationPrincipal Long userId,
                               @Valid @RequestBody ReportReq req) {
        reportService.submit(userId, req); return Result.success("举报已提交，感谢您的反馈");
    }
}
