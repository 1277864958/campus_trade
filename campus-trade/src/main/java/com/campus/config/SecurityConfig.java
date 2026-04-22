package com.campus.config;

import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Spring Security 配置：无状态 JWT 模式
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper  objectMapper;

    private static final String[] WHITE_LIST = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/goods",           // 商品列表公开
            "/api/goods/search",    // 搜索公开
            "/api/goods/{id}",      // 商品详情公开
            "/api/categories",      // 分类公开
            "/files/**",            // 静态文件
            "/ws/**",               // WebSocket
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, resp, e) -> writeJson(resp, 401, Result.unauthorized()))
                .accessDeniedHandler((req, resp, e)      -> writeJson(resp, 403, Result.forbidden())))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

/**
 * JWT 认证过滤器：每个请求进来先验 Token，写入 SecurityContext
 */
@Component
@RequiredArgsConstructor
class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils             jwtUtils;
    private final StringRedisTemplate  redisTemplate;

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(req);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtUtils.validateToken(token)
                        && !Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {

                    Long   userId = jwtUtils.getUserId(token);
                    String role   = jwtUtils.getRole(token);

                    var auth = new UsernamePasswordAuthenticationToken(
                            userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    auth.setDetails(jwtUtils.getUsername(token));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                log.warn("Token 处理异常: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, resp);
    }

    private String extractToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return (StringUtils.hasText(header) && header.startsWith("Bearer "))
                ? header.substring(7) : null;
    }
}
