package com.aims.logic.sdk.controller;

import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LogicBakService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogicBakController {
    private final LogicBakService logicBakService;

    @Autowired
    public LogicBakController(
            LogicBakService _logicBakService) {
        this.logicBakService = _logicBakService;
    }

    @PostMapping("/api/ide/logic-baks")
    public ApiResult<Page<LogicBakEntity>> logicList(@RequestBody FormQueryInput input) {
        var list = this.logicBakService.selectPage(input);
        return new ApiResult<Page<LogicBakEntity>>().setData(list);
    }

    @DeleteMapping("/api/ide/logic-bak/delete/{id}")
    public ApiResult<Boolean> deleteLogic(@PathVariable String id) {
        var res = logicBakService.removeById(id);
        return new ApiResult<Boolean>().setData(res);
    }

    @GetMapping("/api/ide/logic-bak/{id}")
    public ApiResult<LogicBakEntity> getLogic(@PathVariable String id) {
        var entity = logicBakService.getById(id);
        return new ApiResult<LogicBakEntity>().setData(entity);
    }
}
