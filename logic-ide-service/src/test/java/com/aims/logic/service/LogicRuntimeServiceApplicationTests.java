package com.aims.logic.service;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.sdk.LogicRunnerServiceImpl;
import com.aims.logic.service.demo.dto.TestInput;
import com.aims.logic.service.demo.entity.TestEntity;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
class LogicRuntimeServiceApplicationTests {


    @Autowired
    LogicRunnerServiceImpl logic;

//    @Test
    void contextLoads() {
        var env = logic.getEnv();
        env.setLOGIC_CONFIG_MODEL(LogicConfigModelEnum.offline);
        logic.setEnv(JSONObject.from(env), true);
        testObjectArgs();
//        testTran();
    }


//    @Test
    void testTran() {
        TestEntity entity = new TestEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(LocalDateTime.now() + " from test");
        Map<String, Object> pars = new HashMap<>();
        pars.put("entity", entity);
        logic.runByMap("test.tran", pars);
    }

    void testObjectArgs() {
        TestInput entity = new TestInput();
        entity.setI(11);
        entity.setStr("11str");
        entity.setArr(List.of("1", "12"));
        TestInput entity2 = new TestInput();
        entity2.setI(22);
        entity2.setStr("22str");
        entity2.setArr(List.of("21", "22"));
        List<TestInput> entityList = new ArrayList<>();
        entityList.add(entity);
        entityList.add(entity2);
        List<String> strings = List.of("1", "2");
        var res = logic.runBizByObjectArgs("test.objectArgs", UUID.randomUUID().toString(), entityList, 1, strings);
        TestInput input = (TestInput) res.getData();
        System.out.println(">>>testObjectArgs--result");
        System.out.println(res.getMsg());
        System.out.println(res.getData());
        System.out.println(">>>testObjectArgs--result over");
        if (input.str.equals("22str") && input.getI() == 22 && input.getArr().size() == 2) {
            System.out.printf("ok-->testObjectArgs");
        } else {
            System.err.printf("error-->testObjectArgs");
            throw new RuntimeException(">>>testObjectArgs--result");
        }
    }
}
