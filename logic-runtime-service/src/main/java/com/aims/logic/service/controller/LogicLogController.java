package com.aims.logic.service.controller;

import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.service.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class LogicLogController {
    LogicLogMapper logicLogMapper;

    @Autowired
    public LogicLogController(LogicLogMapper _logicLogcMapper) {
        logicLogMapper = _logicLogcMapper;
    }

    @GetMapping("/api/runtime/logic/logs")
    public ApiResult Logs() {
        ApiResult res = new ApiResult();
        res.setData(logicLogMapper.selectList(null));
        return res;
    }
}
