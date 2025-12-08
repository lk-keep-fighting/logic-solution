package com.aims.logic.test.sb2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 2 兼容性测试应用
 */
@SpringBootApplication(scanBasePackages = "com.aims.logic")
public class SpringBoot2TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot2TestApplication.class, args);
    }
}
