package com.aims.logic.sdk;

import com.aims.logic.runtime.service.LogicRunnerService;
import com.alibaba.fastjson2.JSONObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootTest(classes = LogicSdkApplicationTests.class)
@SpringBootApplication(scanBasePackages = "com.aims")
@SpringBootConfiguration
@EnableTransactionManagement
@MapperScan("com.aims.logic.sdk.mapper")
public class LogicSdkApplicationTests {
    @Autowired
    private LogicRunnerService runner;
//    @Autowired
//    private BizLogicRunner bizLogicRunner;

    //    @Test
    void contextLoads() {
        JSONObject cusEnv = new JSONObject();
        JSONObject headers = new JSONObject();
        headers.put("CUSTOME", "SOME VALUE");
        headers.put("wms", "wms config");
        cusEnv.put("HEADERS", headers);
        var res = runner.run("test", null, cusEnv);
//        var res = runner.runBiz("test", "222", null);
//        System.out.printf("user.dir:%s",System.getProperty("user.dir"));
//        mapper.insert(new LogicRuntimeLog().setEnv("java"));
//        testSaveFile();
    }

    //    @Test
    void testRun() {
        var res = runner.runBiz("test", null, null);
        System.out.println(res.getMsg());
    }

    //    @Test
    void testJava() {
        var res = runner.runBiz("test.java", null, null);
        System.out.println(res.getData());
    }

//    @Test
    void testTran() {
        runner.runBizWithTransaction("java.demo", "t11", null, null);
    }
}
