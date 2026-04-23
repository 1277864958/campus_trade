package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.dto.req.*;
import com.campus.dto.resp.*;
import com.campus.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Result<UserResp> register(@Valid @RequestBody RegisterReq req) {
        return Result.success("注册成功", authService.register(req));
    }
    @PostMapping("/login")
    public Result<TokenResp> login(@Valid @RequestBody LoginReq req) {
        return Result.success("登录成功", authService.login(req));
    }
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return Result.success("密码重置成功，请使用新密码登录");
    }
    @PostMapping("/refresh")
    public Result<TokenResp> refresh(@Valid @RequestBody RefreshTokenReq req) {
        return Result.success(authService.refresh(req.getRefreshToken()));
    }
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String header,
                               @AuthenticationPrincipal Long userId) {
        authService.logout(header.substring(7), userId);
        return Result.success("已退出登录");
    }
    @GetMapping("/me")
    public Result<UserResp> me(@AuthenticationPrincipal Long userId) {
        return Result.success(authService.getInfo(userId));
    }
    @PutMapping("/me")
    public Result<UserResp> update(@AuthenticationPrincipal Long userId,
                                   @Valid @RequestBody UpdateUserReq req) {
        return Result.success(authService.updateInfo(userId, req));
    }
    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody ChangePasswordReq req) {
        authService.changePassword(userId, req);
        return Result.success("密码修改成功");
    }
}
