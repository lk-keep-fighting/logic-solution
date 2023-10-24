package com.aims.logic.service.controller;

import com.aims.logic.runtime.contract.log.LogicLog;
import com.aims.logic.service.dto.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/runtime/logic/v1/debug")
public class LogicDebugController {
    @GetMapping("/{id}/lasted-log")
    public ApiResult log(@PathVariable String id) {
        LogicLog log = new LogicLog();
        ApiResult res = new ApiResult().setData(log);
        return res;
    }

    @GetMapping("/{id}/logs")
    public ApiResult logs(@PathVariable String id) {
        LogicLog log = new LogicLog();
        ApiResult res = new ApiResult().setData(log);
        return res;
    }
}
