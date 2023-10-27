package com.aims.logic.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aims")
public class LogicRuntimeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogicRuntimeServiceApplication.class, args);
    }

}
