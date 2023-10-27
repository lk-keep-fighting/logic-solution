package com.aims.logic.ide.service.controller;

import com.aims.logic.ide.service.util.FormSvcUtil;
import com.aims.logic.ide.service.util.dto.FormQueryInput;
import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicMapper;
import com.aims.logic.sdk.service.LogicService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController()
public class LogicController {
    private LogicService logicService;
    private LogicMapper logicMapper;

    @Autowired
    public LogicController(
            LogicService _logicService,
            LogicMapper _logicMapper) {
        this.logicService = _logicService;
        this.logicMapper = _logicMapper;
    }


    @PostMapping("/api/ide/logic/add")
    public boolean addLogic(@RequestBody LogicEntity body) {
        return body.insert();
    }

    @PostMapping("/api/ide/logics")
    public Page<List<LogicEntity>> logicList(@RequestBody PageDTO body) {
        Page<LogicEntity> logicEntityPage = new Page<>(1, 10);
        QueryWrapper queryWrapper = new QueryWrapper();
        return logicMapper.selectPage(logicEntityPage, queryWrapper);
    }

    @PutMapping("/api/ide/logic/edit/{id}")
    public boolean editLogic(@PathVariable String id, @RequestBody LogicEntity body) {
        return body.updateById();
    }

    @DeleteMapping("/api/ide/logic/delete/{id}")
    public int deleteLogic(@PathVariable String id) {
        return logicMapper.deleteById(id);
    }

    @GetMapping("/api/ide/logic/data/{id}")
    public LogicEntity getLogic(@PathVariable String id) {
        var logicEntity = logicMapper.selectById(id);
        return  logicEntity;
    }

    @GetMapping("/api/ide/logic/json/{id}")
    public LogicTreeNode getLogicConfig(@PathVariable String id) {
        var logicEntity = logicMapper.selectById(id);
        if (logicEntity != null) {
            var config = logicEntity.getConfigJson();
            return JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
        }
        return null;
    }

    @PutMapping("/api/ide/logic/config/edit/{id}")
    public int editLogicConfig(@PathVariable String id, @RequestBody String body) {
        return logicMapper.update(null, new UpdateWrapper<LogicEntity>().eq("id", id).set("config_json", body));
    }
}
