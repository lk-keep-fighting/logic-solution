package com.aims.logic.sdk.service.impl;

import com.aims.logic.contract.logger.LogicLog;
import com.aims.logic.contract.dto.LogicRunResult;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoggerServiceImpl {
    private final LogicLogMapper logMapper;
    private final LogicInstanceService instanceService;

    @Autowired
    public LoggerServiceImpl(LogicLogMapper _logMapper,
                             LogicInstanceService _instanceService) {
        this.logMapper = _logMapper;
        instanceService = _instanceService;
    }

    public void addLog(LogicRunResult res) {
        try {
            String env = RuntimeUtil.getEnv().getNODE_ENV();
            LogicLog logicLog = res.getLogicLog();
            var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
            var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
            QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
            Map<String, String> m = new HashMap<>();
            m.put("logicId", logicLog.getLogicId());
            m.put("bizId", logicLog.getBizId());
            q.allEq(m);
            var ins = instanceService.getOne(q);
            if (ins != null) {
                ins.setSuccess(res.isSuccess())
                        .setMessage(res.getMsg())
                        .setReturnData(res.getDataString())
                        .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                        .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                        .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                        .setNextId(nextId)
                        .setNextName(nextName)
                        .setIsOver(logicLog.isOver())
                        .setEnv(env);
                ins.update(q);
            } else {
                ins = new LogicInstanceEntity()
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
                        .setIsOver(logicLog.isOver())
                        .setEnv(env);
                ins.insert();
            }
            JSONObject envJson = logicLog.getEnvsJson();
            JSONObject headers = envJson.getJSONObject("HEADERS");
            String requestHost = null;
            String requestClientId = null;
            String REQUEST_CLIENT_FLAG = envJson.getString("REQUEST_CLIENT_FLAG");
            if (headers != null) {
                requestHost = headers.getString("host");
                requestClientId = headers.getString(REQUEST_CLIENT_FLAG);
            }
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
                    .setIsOver(logicLog.isOver())
                    .setEnv(env)
                    .setHost(requestHost)
                    .setClientId(requestClientId);
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
