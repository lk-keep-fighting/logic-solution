package com.aims.logic.testsuite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.aims"})
@MapperScan("com.aims.logic.testsuite.demo.mapper")
public class TestSuiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestSuiteApplication.class, args);
    }

}
