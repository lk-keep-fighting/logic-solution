package com.aims.logic.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aims")
@MapperScan("com.aims.logic.service.demo.mapper")
@SpringBootConfiguration
public class LogicRuntimeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogicRuntimeServiceApplication.class, args);
    }

}
