package com.aims.logic.ide.controller;

import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LogicLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogicLogController {
    private final LogicLogService logicLogService;

    @Autowired
    public LogicLogController(
            LogicLogService _logService) {
        this.logicLogService = _logService;
    }

    @PostMapping("/api/ide/logic-logs")
    public ApiResult<Page<LogicLogEntity>> logicLogList(@RequestBody FormQueryInput input) {
        var list = this.logicLogService.selectPage(input);
        return new ApiResult<Page<LogicLogEntity>>().setData(list);
    }

    @DeleteMapping("/api/ide/logic-log/delete/{id}")
    public ApiResult<Boolean> deleteLogicLog(@PathVariable String id) {
        var res = logicLogService.removeById(id);
        return new ApiResult<Boolean>().setData(res);
    }

    @GetMapping("/api/ide/logic-log/{id}")
    public ApiResult<LogicLogEntity> getLogicLog(@PathVariable String id) {
        var entity = logicLogService.getById(id);
        return new ApiResult<LogicLogEntity>().setData(entity);
    }

    @DeleteMapping("/api/ide/logic-logs/clear")
    public void clearLogicLog() {
        logicLogService.clearLog();
    }
}
