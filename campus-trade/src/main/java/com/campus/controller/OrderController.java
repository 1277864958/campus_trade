package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.dto.req.*;
import com.campus.dto.resp.*;
import com.campus.service.impl.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public Result<OrderResp> create(@AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody OrderReq req) {
        return Result.success("下单成功", orderService.create(userId, req));
    }
    @GetMapping("/purchases")
    public Result<PageResp<OrderResp>> purchases(@AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.myPurchases(userId, page, size));
    }
    @GetMapping("/sales")
    public Result<PageResp<OrderResp>> sales(@AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.mySales(userId, page, size));
    }
    @PutMapping("/{id}/confirm")
    public Result<OrderResp> confirm(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return Result.success(orderService.confirm(userId, id));
    }
    @PutMapping("/{id}/finish")
    public Result<OrderResp> finish(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return Result.success(orderService.finish(userId, id));
    }
    @PutMapping("/{id}/cancel")
    public Result<OrderResp> cancel(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return Result.success(orderService.cancel(userId, id));
    }
    @PostMapping("/review")
    public Result<ReviewResp> review(@AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody ReviewReq req) {
        return Result.success("评价成功", orderService.submitReview(userId, req));
    }
    @GetMapping("/reviews/{userId}")
    public Result<List<ReviewResp>> userReviews(@PathVariable Long userId) {
        return Result.success(orderService.userReviews(userId));
    }
}
