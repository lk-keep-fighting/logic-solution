package com.aims.logic.sdk;

import com.aims.logic.sdk.util.FileUtil;
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
//        var res = runner.run("test", null);
//        var res = runner.runBiz("test", "222", null);
//        System.out.printf("user.dir:%s",System.getProperty("user.dir"));
//        mapper.insert(new LogicRuntimeLog().setEnv("java"));
        testSaveFile();
    }

    void testSaveFile() {
        try {
            FileUtil.writeFile("logics", "t.json", "{\"d:\":2}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
