package com.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CampusTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusTradeApplication.class, args);
    }
}
