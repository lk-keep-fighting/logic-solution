package com.aims.logic.sdk.controller;

import com.aims.logic.contract.dsl.LogicTreeNode;
import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.mapper.LogicMapper;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.service.LogicService;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LogicIdeController {
    private final LogicMapper logicMapper;
    private final LogicService logicService;
    private final LogicBakService logicBakService;

    @Autowired
    public LogicIdeController(
            LogicMapper logicMapper,
            LogicService logicService,
            LogicBakService logicBakService) {
        this.logicMapper = logicMapper;
        this.logicService = logicService;
        this.logicBakService = logicBakService;
    }


    @PostMapping("/api/ide/logic/add")
    public ApiResult<Boolean> addLogic(@RequestBody LogicEntity body) {
        var res = body.insert();
        return new ApiResult<Boolean>().setData(res);
    }

    @PostMapping("/api/ide/logics")
    public ApiResult<Page<LogicEntity>> logicList(@RequestBody FormQueryInput input) {
        var list = logicService.selectPage(input);
        return new ApiResult<Page<LogicEntity>>().setData(list);
    }

    @PutMapping("/api/ide/logic/edit/{id}")
    public ApiResult<Integer> editLogic(@PathVariable String id, @RequestBody LogicEntity body) {
        var res = logicService.editAndBak(id, body);
        return new ApiResult<Integer>().setData(res);
    }

    @DeleteMapping("/api/ide/logic/delete/{id}")
    public ApiResult<Integer> deleteLogic(@PathVariable String id) {
        var res = logicMapper.deleteById(id);
        return new ApiResult<Integer>().setData(res);
    }

    @GetMapping("/api/ide/logic/{id}")
    public ApiResult<LogicEntity> getLogic(@PathVariable String id) {
        var logicEntity = logicMapper.selectById(id);
        return new ApiResult<LogicEntity>().setData(logicEntity);
    }

    @GetMapping("/api/ide/logic/{id}/config")
    public ApiResult<LogicTreeNode> getLogicConfig(@PathVariable String id) {
        var logicEntity = logicMapper.selectById(id);
        if (logicEntity != null) {
            var config = logicEntity.getConfigJson();
            var res = JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
            return new ApiResult<LogicTreeNode>().setData(res);
        }
        return new ApiResult<LogicTreeNode>();
    }

    @GetMapping("/api/ide/logic/{id}/config/{version}")
    public ApiResult<LogicTreeNode> getLogicConfigByVersion(@PathVariable String id, @PathVariable String version) {
        QueryWrapper<LogicBakEntity> queryWrapper = new QueryWrapper<>();
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("version", version);
        queryWrapper.allEq(map);
        var logicBakEntityEntity = logicBakService.getOne(queryWrapper);
        if (logicBakEntityEntity != null) {
            var config = logicBakEntityEntity.getConfigJson();
            var res = JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
            return new ApiResult<LogicTreeNode>().setData(res);
        }
        return new ApiResult<>();
    }

}
