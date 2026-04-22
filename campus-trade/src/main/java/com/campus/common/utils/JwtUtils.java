package com.campus.common.utils;

import com.campus.common.exception.GlobalExceptionHandler;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（Spring Bean，由配置文件注入密钥）
 */

@Component
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private final SecretKey secretKey;
    private final long      expireMs;
    private final long      refreshExpireMs;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expire-minutes:30}") int expireMinutes,
            @Value("${jwt.refresh-expire-days:7}") int refreshDays) {
        this.secretKey       = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs        = (long) expireMinutes * 60 * 1000;
        this.refreshExpireMs = (long) refreshDays   * 24 * 3600 * 1000;
    }

    /** 生成访问 Token */
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    /** 生成刷新 Token */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpireMs))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public long getExpireMs() { return expireMs; }

    public boolean validateToken(String token) {
        try { parseToken(token); return true; }
        catch (ExpiredJwtException e) { log.warn("JWT 已过期"); }
        catch (JwtException e)        { log.warn("JWT 无效: {}", e.getMessage()); }
        return false;
    }
}
