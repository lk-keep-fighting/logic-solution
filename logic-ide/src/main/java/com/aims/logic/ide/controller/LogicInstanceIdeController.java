package com.aims.logic.ide.controller;

import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/api/ide/logic-instance/edit/{id}/version")
    public ApiResult editLogicVersion(@PathVariable String id, @RequestBody LogicInstanceEntity entity) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("version", entity.getVersion());
        return new ApiResult().setData(instanceService.updateById(id, valuesMap));
    }

    @PostMapping("/api/ide/logic-instance/edit/{id}/paramsJson")
    public ApiResult editLogicParamsJson(@PathVariable String id, @RequestBody LogicInstanceEntity entity) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("paramsJson", entity.getParamsJson());
        return new ApiResult().setData(this.instanceService.updateById(id, valuesMap));
    }

    @DeleteMapping("/api/ide/logic-instance/delete/{id}")
    public ApiResult deleteLogic(@PathVariable String id) {
        var res = instanceService.removeById(id);
        return new ApiResult().setData(res);
    }

    @DeleteMapping("/api/ide/logic-instance/batch-delete")
    public ApiResult deleteLogic(@RequestBody List<String> ids) {
        var res = instanceService.removeByIds(ids);
        return new ApiResult().setData(res);
    }

    @GetMapping("/api/ide/logic-instance/{id}")
    public ApiResult<LogicInstanceEntity> getLogic(@PathVariable String id) {
        var entity = instanceService.selectById(id);
        return new ApiResult<LogicInstanceEntity>().setData(entity);
    }

    @GetMapping("/api/ide/logic-instance/{logicId}/{bizId}")
    public ApiResult<LogicInstanceEntity> getLogicByBizId(@PathVariable String logicId, @PathVariable String bizId) {
        var res = instanceService.getInstance(logicId, bizId);
        return new ApiResult<LogicInstanceEntity>().setData(res);
    }
}
