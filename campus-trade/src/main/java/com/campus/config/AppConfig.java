package com.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Redis 序列化配置（key/value 均用 String）
 */
@Configuration
class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(factory);
        StringRedisSerializer ser = new StringRedisSerializer();
        tpl.setKeySerializer(ser);
        tpl.setValueSerializer(ser);
        tpl.setHashKeySerializer(ser);
        tpl.setHashValueSerializer(ser);
        tpl.afterPropertiesSet();
        return tpl;
    }
}

/**
 * MVC 配置：CORS 跨域 + 静态文件访问
 */
@Configuration
class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** 允许前端开发服务器跨域（生产环境改为具体域名） */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 将 /files/** 映射到本地上传目录，实现图片访问
     * 例：http://localhost:8080/files/2024/01/01/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
