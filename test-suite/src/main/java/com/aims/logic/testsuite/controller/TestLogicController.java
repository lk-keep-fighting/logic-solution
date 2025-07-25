package com.aims.logic.testsuite.controller;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.service.LogicRunnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class TestLogicController {
    public TestLogicController() {
    }

    @Autowired
    LogicRunnerService logicRunnerService;

    @RequestMapping("/api/test/run/{logicId}/{bizId}")
    public LogicRunResult testLogic(@PathVariable("logicId") String logicId, @PathVariable("bizId") String bizId, @RequestBody String json) {
        return logicRunnerService.runBizByObjectArgs(logicId, bizId, json);
    }

    @PostMapping("/api/test/run-async/{logicId}/{bizId}")
    @Async
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void testLogicAsync(
            @PathVariable("logicId") String logicId,
            @PathVariable("bizId") String bizId,
            @RequestBody String json) {
        log.info("Starting async execution for logicId: {}, bizId: {}", logicId, bizId);
        try {
            logicRunnerService.runBizByObjectArgs(logicId, bizId, json);
        } catch (Exception e) {
            log.error("Error during async execution for logicId: {}, bizId: {}", logicId, bizId, e);
            throw e;
        }
    }
}