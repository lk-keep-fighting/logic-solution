package com.aims.logic.testsuite;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class TestSuitApplicationTests {

    @Autowired
    LogicRunnerService logic;

    @Test
    void contextLoads() {
        testBatchTran(1);
    }

    void testBatchTran(int batchSize) {
        System.out.println(">>testBatchTran>>");
        Date start = new Date();
        System.out.println(">>testBatchTran>>");
        ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(batchSize);
        System.out.println(">>testBatchTran>>开始时间：" + start.getTime());
        var cusEnv = logic.getEnv();
        cusEnv.setLOGIC_CONFIG_MODEL(LogicConfigModelEnum.offline);
        var offlineLogic = logic.newInstance(JSONObject.from(cusEnv));
        offlineLogic.getEnv().setLOGIC_CONFIG_MODEL(LogicConfigModelEnum.offline);
        for (int i = 0; i < batchSize; i++) {
            final String FinalI = String.valueOf(i);
            Runnable runnable = () -> {
                String id = String.valueOf(new Random().nextInt());
                String bizId = String.valueOf(new Date().getTime()) + FinalI;
                testTran(offlineLogic, bizId, id);
            };
            executorService.submit(runnable);
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Date end = new Date();
        System.out.println(">>testBatchTran>>批次大小：" + batchSize + "，耗时：" + (end.getTime() - start.getTime()) + "ms");
    }

    void testTran(LogicRunnerService offlineLogic, String bizId, String id) {
        var res = offlineLogic.runBizByObjectArgs("test.tran", bizId, id);
        if (!res.isSuccess()) {
            var newId = id + 1;
            var uptRes = offlineLogic.updateBizInstanceParams("test.tran", bizId, newId);
            if (uptRes) {
                res = offlineLogic.retryErrorBiz("test.tran", bizId);
                if (res.isSuccess()) {
                    System.out.println(">>test ok>>testTran");
                } else {
                    System.err.printf("返回值：%s,消息：%s%n", res.getDataString(), res.getMsg());
//                    throw new RuntimeException("retry error biz failed");
                }
            } else {
                throw new RuntimeException("update biz instance params failed");
            }
        } else {
            throw new RuntimeException("测试事务存在问题，应该失败，但成功了！");
        }
    }
}
