package com.campus.config;

import com.campus.entity.User;
import com.campus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123456";

    @Override
    public void run(String... args) {
        userRepo.findByUsername(ADMIN_USERNAME).ifPresentOrElse(
                admin -> {
                    if (!encoder.matches(ADMIN_PASSWORD, admin.getPasswordHash())) {
                        admin.setPasswordHash(encoder.encode(ADMIN_PASSWORD));
                        userRepo.save(admin);
                        log.info("admin 密码已重置为默认密码");
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setUsername(ADMIN_USERNAME);
                    admin.setPasswordHash(encoder.encode(ADMIN_PASSWORD));
                    admin.setNickname("系统管理员");
                    admin.setRole("ADMIN");
                    admin.setStatus(1);
                    userRepo.save(admin);
                    log.info("admin 账号已创建");
                }
        );
    }
}
