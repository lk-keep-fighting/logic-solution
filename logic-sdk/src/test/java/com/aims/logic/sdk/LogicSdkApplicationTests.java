package com.aims.logic.sdk;

import com.aims.logic.sdk.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Scope;

@SpringBootTest()
@SpringBootApplication(scanBasePackages = "com.aims")
@SpringBootConfiguration
@MapperScan("com.aims.logic.sdk.mapper")
public class LogicSdkApplicationTests {
    @Autowired
    private LogicRunner runner;
    @Autowired
    private BizLogicRunner bizLogicRunner;
//    @Autowired
//    LogicRuntimeLogMapper mapper;

    @Test
    void contextLoads() {
        var res = runner.run("test", null);
//        var res = runner.runBiz("test", "222", null);
//        System.out.printf("user.dir:%s",System.getProperty("user.dir"));
//        mapper.insert(new LogicRuntimeLog().setEnv("java"));
//        testSaveFile();
    }

    @Test
    void testRun() {
        var res = bizLogicRunner.runBiz("test", "224", null);
        System.out.println(res.getMsg());
    }

    void testSaveFile() {
        try {
            FileUtil.writeFile("logics", "t.json", "{\"d:\":3}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
