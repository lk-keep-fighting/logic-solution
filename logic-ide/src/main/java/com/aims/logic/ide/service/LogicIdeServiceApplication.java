package com.aims.logic.ide.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aims.logic")
public class LogicIdeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogicIdeServiceApplication.class, args);
    }

}
