package com.aims.logic.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.aims.logic", "com.aims.datamodel"})
@SpringBootConfiguration
public class LogicIdeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogicIdeServiceApplication.class, args);
    }

}
