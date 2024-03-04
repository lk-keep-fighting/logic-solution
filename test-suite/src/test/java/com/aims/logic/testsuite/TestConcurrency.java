package com.aims.logic.testsuite;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.service.LogicRunnerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class TestConcurrency {
    @Autowired
    LogicRunnerService logic;

    @Test
    public void test() throws InterruptedException {
        logic.getEnv().setLOGIC_CONFIG_MODEL(LogicConfigModelEnum.offline);
        String bizId = String.valueOf(new Date().getTime());
//        var res = logic.runBizByObjectArgs("test.pub", bizId);
//        log.info("res.getDataString()");
//        log.info(res.getDataString());
        ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Runnable runnable = () -> {
                try {
                    var res = logic.runBizByObjectArgs("test.pub", bizId + String.valueOf(finalI / 2), String.valueOf(finalI));
                    log.info("res.getDataString()");
                    log.info(res.getMsg());
                    log.info(res.getDataString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
    }
}
