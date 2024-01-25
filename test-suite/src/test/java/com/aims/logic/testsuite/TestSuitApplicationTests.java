package com.aims.logic.testsuite;

import com.aims.logic.runtime.service.LogicRunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Random;

@SpringBootTest
class TestSuitApplicationTests {

    @Autowired
    LogicRunnerService logic;

//    @Test
    void contextLoads() {
        testBatchTran(10);
    }

    void testBatchTran(int batchSize) {
        System.out.println(">>testBatchTran>>");
        Date start = new Date();
        System.out.println(">>testBatchTran>>");
        for (int i = 0; i < batchSize; i++) {
            testTran();
        }
        Date end = new Date();
        System.out.println(">>testBatchTran>>批次大小：" + batchSize + "，耗时：" + (end.getTime() - start.getTime()) + "ms");
    }

    void testTran() {
        String id = String.valueOf(new Random().nextInt());
        var bizId = String.valueOf(new Date().getTime());
        var res = logic.runBizByObjectArgs("test.tran", bizId, id);
        if (!res.isSuccess()) {
            var newId = id + 1;
            var uptRes = logic.updateBizInstanceParams("test.tran", bizId, newId);
            if (uptRes) {
                res = logic.retryErrorBiz("test.tran", bizId);
                if (res.isSuccess()) {
                    System.out.println(">>test ok>>testTran");
                } else {
                    System.err.printf("返回值：%s,消息：%s%n", res.getDataString(), res.getMsg());
                    throw new RuntimeException("retry error biz failed");
                }
            } else {
                throw new RuntimeException("update biz instance params failed");
            }
        } else {
            throw new RuntimeException("测试事务存在问题，应该失败，但成功了！");
        }
    }
}
