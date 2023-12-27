package com.aims.logic.ide.controller;

import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogicInstanceIdeController {
    private final LogicInstanceService instanceService;
    @Autowired
    public LogicInstanceIdeController(
            LogicInstanceService _insService) {
        this.instanceService = _insService;
    }

    @PostMapping("/api/ide/logic-instances")
    public ApiResult<Page<LogicInstanceEntity>> logicList(@RequestBody FormQueryInput input) {
        var list = this.instanceService.selectPage(input);
        return new ApiResult<Page<LogicInstanceEntity>>().setData(list);
    }

    @DeleteMapping("/api/ide/logic-instance/delete/{id}")
    public ApiResult<Boolean> deleteLogic(@PathVariable String id) {
        var res = instanceService.removeById(id);
        return new ApiResult<Boolean>().setData(res);
    }

    @GetMapping("/api/ide/logic-instance/{id}")
    public ApiResult<LogicInstanceEntity> getLogic(@PathVariable String id) {
        var entity = instanceService.getById(id);
        return new ApiResult<LogicInstanceEntity>().setData(entity);
    }
}
