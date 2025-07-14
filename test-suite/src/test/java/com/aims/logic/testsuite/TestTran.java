package com.aims.logic.testsuite;

import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.testsuite.demo.TestTranService;
import com.aims.logic.testsuite.demo.entity.TestEntity;
import com.aims.logic.testsuite.demo.mapper.TestMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
public class TestTran {
    @Autowired
    LogicRunnerService logic;
    @Autowired
    TestMapper testMapper;
    @Autowired
    TestTranService testTranService;

    @Test
    @Transactional
    void test() {
        testMapper.insert(new TestEntity().setId("1"));
        var res = logic.runBizByObjectArgs("tran1", "99", new Object[]{"2"});
        System.out.println(res.getDataString());
    }

    @Test
    void test2() {
        testTranService.insertWithInnerTran("201");
    }

}
