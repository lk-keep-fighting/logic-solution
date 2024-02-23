package com.aims.logic.ide.controller;

import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResult<Boolean> editLogicVersion(@PathVariable String id, @RequestBody LogicInstanceEntity entity) {
        UpdateWrapper<LogicInstanceEntity> wrapper = new UpdateWrapper();
        wrapper.eq("id", id).set("version", entity.getVersion());
        return new ApiResult<Boolean>().setData(this.instanceService.update(null, wrapper));
    }

    @PostMapping("/api/ide/logic-instance/edit/{id}/paramsJson")
    public ApiResult<Boolean> editLogicParamsJson(@PathVariable String id, @RequestBody LogicInstanceEntity entity) {
        UpdateWrapper<LogicInstanceEntity> wrapper = new UpdateWrapper();
        wrapper.eq("id", id).set("paramsJson", entity.getParamsJson());
        return new ApiResult<Boolean>().setData(this.instanceService.update(null, wrapper));
    }

    @DeleteMapping("/api/ide/logic-instance/delete/{id}")
    public ApiResult<Boolean> deleteLogic(@PathVariable String id) {
        var res = instanceService.removeById(id);
        return new ApiResult<Boolean>().setData(res);
    }

    @DeleteMapping("/api/ide/logic-instance/batch-delete")
    public ApiResult<Boolean> deleteLogic(@RequestBody List<String> ids) {
        var res = instanceService.removeByIds(ids);
        return new ApiResult<Boolean>().setData(res);
    }

    @GetMapping("/api/ide/logic-instance/{id}")
    public ApiResult<LogicInstanceEntity> getLogic(@PathVariable String id) {
        var entity = instanceService.getById(id);
        return new ApiResult<LogicInstanceEntity>().setData(entity);
    }
}
