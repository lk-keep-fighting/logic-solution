package com.aims.logic.service;

import com.aims.logic.sdk.LogicRunnerServiceImpl;
import com.aims.logic.service.demo.entity.TestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
class LogicRuntimeServiceApplicationTests {

    //    @Test
    void contextLoads() {
    }

    @Autowired
    LogicRunnerServiceImpl logic;

    //    @Test
    void testTran() {
        TestEntity entity = new TestEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(LocalDateTime.now() + " from test");
        Map<String, Object> pars = new HashMap<>();
        pars.put("entity", entity);
        logic.runByMap("test.tran", pars);
    }

}
