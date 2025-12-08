package com.aims.logic.test.sb3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 3 兼容性测试应用
 */
@SpringBootApplication(scanBasePackages = "com.aims.logic")
public class SpringBoot3TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot3TestApplication.class, args);
    }
}
