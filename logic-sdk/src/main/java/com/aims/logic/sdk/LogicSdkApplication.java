package com.aims.logic.sdk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(scanBasePackages = "com.aims")
//@SpringBootConfiguration
//@MapperScan("com.aims.logic.sdk.mapper")
public class LogicSdkApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogicSdkApplication.class, args);
    }
}
