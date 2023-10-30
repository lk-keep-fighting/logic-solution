package com.aims.logic.sdk.service;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.enums.EnvEnum;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoggerService {
    private final LogicLogMapper logMapper;
    private final LogicInstanceMapper instanceMapper;
    private final LogicInstanceService instanceService;

    @Autowired
    public LoggerService(LogicLogMapper _logMapper,
                         LogicInstanceMapper _instanceMapper,
                         LogicInstanceService _instanceService) {
        this.logMapper = _logMapper;
        this.instanceMapper = _instanceMapper;
        instanceService = _instanceService;
    }

    public void addLog(LogicRunResult res) {
        try {
            String env = RuntimeUtil.getEnv().getNODE_ENV();
            com.aims.logic.runtime.contract.logger.LogicLog logicLog = res.getLogicLog();
            var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
            var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
            LogicInstanceEntity instanceEntity = new LogicInstanceEntity()
                    .setSuccess(res.isSuccess())
                    .setMessage(res.getMsg())
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setReturnData(res.getDataString())
                    .setLogicId(logicLog.getLogicId())
                    .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .setNextId(nextId)
                    .setNextName(nextName)
                    .setOver(logicLog.isOver())
                    .setEnv(env);
            QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
            Map<String, String> m = new HashMap<>();
            m.put("logicId", logicLog.getLogicId());
            m.put("bizId", logicLog.getBizId());
            q.allEq(m);
            instanceService.saveOrUpdate(instanceEntity, q);

            LogicLogEntity logEntity = new LogicLogEntity()
                    .setSuccess(res.isSuccess())
                    .setMessage(res.getMsg())
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setItemLogs(JSON.toJSONString(logicLog.getItemLogs()))
                    .setReturnData(res.getDataString())
                    .setLogicId(logicLog.getLogicId())
                    .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .setNextId(nextId)
                    .setNextName(nextName)
                    .setOver(logicLog.isOver())
                    .setEnv(env);
            logMapper.insert(logEntity);
        } catch (Exception ex) {
            System.err.println("添加日志异常");
            System.err.println(ex);
        }

    }

    public List<LogicLogEntity> queryLogs(String logicId) {
        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("logicId", logicId);
        wrapper.orderByDesc("id");
        return logMapper.selectList(wrapper);
    }

    public LogicLogEntity findLastBizLog(String logicId, String bizId) {
        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("logicId", logicId);
        wrapper.eq("bizId", bizId);
        wrapper.orderByDesc("serverTime").last("LIMIT 1");
        return logMapper.selectOne(wrapper);
    }

    public int deleteLog(long aid) {
        return logMapper.deleteById(aid);
    }
}
