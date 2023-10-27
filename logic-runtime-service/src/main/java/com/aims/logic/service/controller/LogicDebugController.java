package com.aims.logic.service.controller;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.service.dto.ApiResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogicDebugController {
    private final LogicLogMapper logicLogMapper;

    public LogicDebugController(LogicLogMapper _logicLogMapper) {
        this.logicLogMapper = _logicLogMapper;
    }

    @GetMapping("/api/runtime/logic/v1/debug/{logicId}/lasted-log")
    public ApiResult log(@PathVariable String logicId) {
        QueryWrapper<LogicLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("logic_id", logicId)
                .orderByDesc("server_time")
                .last("limit 1");
        var lastedLog = logicLogMapper
                .selectList(queryWrapper).stream().findFirst();
        ApiResult res = new ApiResult().setData(lastedLog);
        return res;
    }

    @GetMapping("/api/runtime/logic/v1/debug/{logicId}/logs")
    public ApiResult logs(@PathVariable String logicId) {
        QueryWrapper<LogicLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("logic_id", logicId)
                .orderByDesc("server_time")
                .last("limit 1");
        var list = logicLogMapper
                .selectList(queryWrapper);
        ApiResult res = new ApiResult().setData(list);
        return res;
    }
}
