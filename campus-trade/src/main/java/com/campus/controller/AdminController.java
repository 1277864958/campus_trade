package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.dto.resp.*;
import com.campus.dto.req.GoodsSearchReq;
import com.campus.service.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService    adminService;
    private final GoodsService    goodsService;
    private final ReportService   reportService;
    private final CategoryService categoryService;

    @GetMapping("/stat")
    public Result<AdminStatResp> stat() { return Result.success(adminService.stat()); }

    @GetMapping("/users")
    public Result<PageResp<UserResp>> users(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.users(page, size));
    }
    @PutMapping("/users/{id}/status")
    public Result<Void> setStatus(@PathVariable Long id, @RequestParam int status) {
        adminService.setStatus(id, status);
        return Result.success(status == 1 ? "账号已启用" : "账号已禁用");
    }
    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        adminService.resetPassword(id, newPassword); return Result.success("密码已重置");
    }
    @GetMapping("/goods")
    public Result<PageResp<GoodsResp>> goods(GoodsSearchReq req) {
        return Result.success(goodsService.adminSearch(req));
    }
    @PutMapping("/goods/{id}/take-down")
    public Result<Void> takeDown(@PathVariable Long id) {
        goodsService.adminTakeDown(id); return Result.success("已强制下架");
    }
    @GetMapping("/reports")
    public Result<PageResp<ReportResp>> reports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(reportService.adminList(status, page, size));
    }
    @PutMapping("/reports/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id, @RequestParam String action) {
        reportService.handle(id, action); return Result.success("处理成功");
    }
    @PostMapping("/categories")
    public Result<CategoryResp> createCategory(
            @RequestParam String name, @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String icon, @RequestParam(required = false) Integer sort) {
        return Result.success(categoryService.create(name, parentId, icon, sort));
    }
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id); return Result.success("删除成功");
    }
}
