package com.campus.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.common.utils.JwtUtils;
import com.campus.dto.req.*;
import com.campus.dto.resp.*;
import com.campus.entity.User;
import com.campus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务：注册 / 登录 / 刷新 Token / 登出 / 改密
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepo;
    private final PasswordEncoder     encoder;
    private final JwtUtils            jwtUtils;
    private final StringRedisTemplate redis;
    private final CaptchaService      captchaService;

    private static final String REFRESH_PREFIX   = "auth:refresh:";
    private static final String BLACKLIST_PREFIX  = "auth:blacklist:";
    private static final long   REFRESH_EXPIRE_D  = 7L;

    // ── 注册 ─────────────────────────────────────────────────
    @Transactional
    public UserResp register(RegisterReq req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw BusinessException.of("用户名已被占用");
        if (req.getPhone() != null && userRepo.existsByPhone(req.getPhone()))
            throw BusinessException.of("手机号已被注册");

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        userRepo.save(u);
        log.info("新用户注册: {}", u.getUsername());
        return toResp(u, null);
    }

    // ── 登录 ─────────────────────────────────────────────────
    public TokenResp login(LoginReq req) {
        if (!captchaService.verify(req.getCaptchaId(), req.getCaptchaCode()))
            throw BusinessException.of("验证码错误或已过期");

        User u = userRepo.findByUsername(req.getUsername())
                .or(() -> userRepo.findByPhone(req.getUsername()))
                .orElseThrow(() -> BusinessException.of("账号或密码错误"));

        if (!encoder.matches(req.getPassword(), u.getPasswordHash()))
            throw BusinessException.of("账号或密码错误");
        if (u.getStatus() == 0)
            throw BusinessException.of("账号已被禁用，请联系管理员");

        return buildTokenResp(u);
    }

    // ── 刷新 Token ───────────────────────────────────────────
    public TokenResp refresh(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken))
            throw BusinessException.of("Refresh token 无效或已过期");

        Long userId = jwtUtils.getUserId(refreshToken);
        String stored = redis.opsForValue().get(REFRESH_PREFIX + userId);
        if (!refreshToken.equals(stored))
            throw BusinessException.of("Refresh token 已失效，请重新登录");

        User u = userRepo.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        redis.delete(REFRESH_PREFIX + userId);
        return buildTokenResp(u);
    }

    // ── 登出 ─────────────────────────────────────────────────
    public void logout(String accessToken, Long userId) {
        redis.delete(REFRESH_PREFIX + userId);
        long remainSec = jwtUtils.getExpireMs() / 1000;
        redis.opsForValue().set(BLACKLIST_PREFIX + accessToken, "1", remainSec, TimeUnit.SECONDS);
    }

    // ── 修改密码 ──────────────────────────────────────────────
    @Transactional
    public void changePassword(Long userId, ChangePasswordReq req) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (!encoder.matches(req.getOldPassword(), u.getPasswordHash()))
            throw BusinessException.of("旧密码错误");
        u.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(u);
    }

    // ── 忘记密码（通过用户名+手机号验证身份）─────────────────
    @Transactional
    public void resetPassword(ResetPasswordReq req) {
        if (!captchaService.verify(req.getCaptchaId(), req.getCaptchaCode()))
            throw BusinessException.of("验证码错误或已过期");

        User u = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> BusinessException.of("用户不存在"));

        if ("ADMIN".equals(u.getRole()))
            throw BusinessException.of("管理员账号不支持此操作，请联系系统管理员");

        if (u.getPhone() == null || u.getPhone().isBlank())
            throw BusinessException.of("该账号未绑定手机号，无法重置密码");

        if (!u.getPhone().equals(req.getPhone()))
            throw BusinessException.of("手机号与注册时不一致");

        u.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(u);
        log.info("用户 {} 通过忘记密码重置了密码", u.getUsername());
    }

    // ── 获取用户信息 ──────────────────────────────────────────
    public UserResp getInfo(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        return toResp(u, null);
    }

    // ── 更新用户信息 ──────────────────────────────────────────
    @Transactional
    public UserResp updateInfo(Long userId, UpdateUserReq req) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (req.getNickname()  != null) u.setNickname(req.getNickname());
        if (req.getAvatarUrl() != null) u.setAvatarUrl(req.getAvatarUrl());
        if (req.getPhone()     != null) u.setPhone(req.getPhone());
        if (req.getEmail()     != null) u.setEmail(req.getEmail());
        return toResp(userRepo.save(u), null);
    }

    // ── 私有辅助 ─────────────────────────────────────────────
    private TokenResp buildTokenResp(User u) {
        String access  = jwtUtils.generateToken(u.getId(), u.getUsername(), u.getRole());
        String refresh = jwtUtils.generateRefreshToken(u.getId());
        redis.opsForValue().set(REFRESH_PREFIX + u.getId(), refresh,
                REFRESH_EXPIRE_D, TimeUnit.DAYS);
        return new TokenResp(access, refresh, u.getId(), u.getUsername(),
                u.getRole(), jwtUtils.getExpireMs() / 1000);
    }

    public static UserResp toResp(User u, Double avgScore) {
        UserResp r = new UserResp();
        BeanUtils.copyProperties(u, r);
        r.setAvgScore(avgScore);
        return r;
    }
}
