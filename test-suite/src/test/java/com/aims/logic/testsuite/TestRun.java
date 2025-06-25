package com.aims.logic.testsuite;

import com.aims.logic.runtime.service.LogicRunnerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class TestRun {
    @Autowired
    LogicRunnerService logic;

    @Test
    public void test() {
        logic.runByJson("t", "{\"name\":\"test_name\"}");
    }
}
