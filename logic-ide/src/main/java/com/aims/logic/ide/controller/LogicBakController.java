package com.aims.logic.ide.controller;

import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.service.LogicBakService;
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
    public ApiResult deleteLogic(@PathVariable String id) {
        var res = logicBakService.removeById(id);
        return new ApiResult().setData(res);
    }

    @GetMapping("/api/ide/logic-bak/{id}")
    public ApiResult<LogicBakEntity> getLogic(@PathVariable String id) {
        var entity = logicBakService.selectById(id);
        return new ApiResult<LogicBakEntity>().setData(entity);
    }
}
