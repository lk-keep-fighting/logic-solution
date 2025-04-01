package com.aims.logic.testsuite;

import com.aims.logic.runtime.service.LogicRunnerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@Slf4j
@SpringBootTest
public class TestQuerySDK {
    @Autowired
    LogicRunnerService logic;

    @Test
    public void contextLoads() {
        var res = logic.queryUncompletedBiz(null, null, null);
        assert !res.isEmpty();
        res = logic.queryUncompletedBiz(null, LocalDateTime.now(), null);
        assert !res.isEmpty();
        res = logic.queryUncompletedBiz(LocalDateTime.now().minusHours(1), LocalDateTime.now(), null);
        assert !res.isEmpty();
        res = logic.queryUncompletedBiz(LocalDateTime.now().minusMinutes(1), LocalDateTime.now(), false);
        assert !res.isEmpty();
        res = logic.queryUncompletedBiz(LocalDateTime.now().minusHours(1), LocalDateTime.now(), true);
        assert res.isEmpty();

    }
}
