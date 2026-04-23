package com.campus.controller;

import com.campus.common.result.Result;
import com.campus.service.impl.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping
    public Result<CaptchaService.CaptchaResult> getCaptcha() {
        return Result.success(captchaService.generate());
    }
}
