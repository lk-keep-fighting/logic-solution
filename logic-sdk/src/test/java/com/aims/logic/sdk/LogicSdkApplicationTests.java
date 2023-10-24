package com.aims.logic.sdk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LogicSdkApplicationTests {
    @Autowired
    private LogicRunner runner;
//    @Autowired
//    LogicRuntimeLogMapper mapper;

    @Test
    void contextLoads() {
        var res = runner.run("test", null);
        System.out.println(res);
//        mapper.insert(new LogicRuntimeLog().setEnv("java"));
    }

}
