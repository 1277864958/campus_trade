package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.service.impl.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<?> tree() { return Result.success(categoryService.tree()); }
}
