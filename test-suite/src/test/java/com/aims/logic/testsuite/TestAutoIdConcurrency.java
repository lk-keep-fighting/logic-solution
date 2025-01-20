package com.aims.logic.testsuite;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.testsuite.demo.entity.TestAutoIdEntity;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class TestAutoIdConcurrency {
    @Autowired
    LogicRunnerService logic;
    @Test
    void contextLoads() {
        testBatchTran(10);
    }

    void testBatchTran(int batchSize) {
        System.out.println(">>testAutoIdBatchTran>>");
        Date start = new Date();
        System.out.println(">>testAutoIdBatchTran>>");
        ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(batchSize);
        System.out.println(">>testAutoIdBatchTran>>开始时间：" + start.getTime());
        var cusEnv = logic.getEnv();
        cusEnv.setLOGIC_CONFIG_MODEL(LogicConfigModelEnum.offline);
        var offlineLogic = logic.newRunnerService(JSONObject.from(cusEnv));
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
        System.out.println(">>testAutoIdBatchTran>>批次大小：" + batchSize + "，耗时：" + (end.getTime() - start.getTime()) + "ms");
    }

    void testTran(LogicRunnerService offlineLogic, String bizId, String id) {
//        var res = offlineLogic.runBizByObjectArgs("test.tran", bizId, id);
        var res = offlineLogic.runBizByObjectArgs("test.autoId", bizId, new TestAutoIdEntity().setName(bizId));
        if (!res.isSuccess()) {
            throw new RuntimeException("isSuccess=false");
        } else {
            log.info(bizId + "-ok");
        }
    }
}
