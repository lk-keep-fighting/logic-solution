package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
        this.instanceService = _instanceService;
    }

    /**
     * 添加实例和日志聚合方法,添加出错不会抛出异常，会在控制台打印
     * 1、addOrUpdateInstance新增或更新运行实例日志；
     * 2、addLogicLog新增执行日志logic_log日志
     *
     * @param logicLog
     */
    public void addOrUpdateInstanceAndAddLogicLog(LogicLog logicLog) {
        addOrUpdateInstance(logicLog);
        try {
            addLogicLog(logicLog);
        } catch (Exception ex) {
            System.err.println("添加日志异常");
            System.err.println(ex);
        }
    }

    public void updateInstanceStatus(String instanceId, boolean success, String msg) {
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("id", instanceId);
        updateWrapper.set("success", success);
        var msg255 = msg == null ? null : msg.length() > 255 ? msg.substring(0, 255) : msg;
        updateWrapper.set("message", msg255);
        instanceService.update(null, updateWrapper);
    }

    /**
     * 新增或更新运行实例日志
     *
     * @param logicLog
     */
    public void addOrUpdateInstance(LogicLog logicLog) {
        String env = RuntimeUtil.getEnvObject().getNODE_ENV();
        var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
        var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
//        QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
//        Map<String, String> m = new HashMap<>();
//        m.put("logicId", logicLog.getLogicId());
//        m.put("bizId", logicLog.getBizId());
//        q.allEq(m);
//        var ins = instanceService.getOne(q);
        var msg255 = logicLog.getMsg() == null ? null : logicLog.getMsg().length() > 255 ? logicLog.getMsg().substring(0, 255) : logicLog.getMsg();
        if (logicLog.getInstanceId() != null) {
            LambdaUpdateWrapper<LogicInstanceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(LogicInstanceEntity::getSuccess, logicLog.isSuccess())
                    .set(LogicInstanceEntity::getMessage, msg255)
                    .set(LogicInstanceEntity::getReturnData, logicLog.getReturnDataStr() != null ? logicLog.getReturnDataStr() : null)
                    .set(LogicInstanceEntity::getParamsJson, logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .set(LogicInstanceEntity::getVarsJson, logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .set(LogicInstanceEntity::getVarsJsonEnd, logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .set(LogicInstanceEntity::getIsOver, logicLog.isOver()).set(LogicInstanceEntity::getNextId, nextId)
                    .set(LogicInstanceEntity::getNextName, nextName)
                    .set(LogicInstanceEntity::getEnv, env)
                    .eq(LogicInstanceEntity::getId, logicLog.getInstanceId());
            instanceService.update(updateWrapper);
        } else {
            LogicInstanceEntity newIns = new LogicInstanceEntity()
                    .setSuccess(logicLog.isSuccess())
                    .setMessage(msg255)
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setReturnData(logicLog.getReturnDataStr() != null ? logicLog.getReturnDataStr() : null)
                    .setLogicId(logicLog.getLogicId())
                    .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .setEnvsJson(logicLog.getEnvsJson() == null ? null : logicLog.getEnvsJson().toJSONString())
                    .setIsOver(logicLog.isOver())
                    .setNextId(nextId)
                    .setNextName(nextName)
                    .setEnv(env);
//            newIns.insert();
            instanceService.save(newIns);
            logicLog.setInstanceId(newIns.getId());
        }
    }

    /**
     * 新增执行日志logic_log日志
     *
     * @param logicLog
     */
    public void addLogicLog(LogicLog logicLog) {
        JSONObject envJson = logicLog.getEnvsJson();
        JSONObject headers = envJson.getJSONObject("HEADERS");
        String requestHost = null;
        String requestClientId = null;
        String REQUEST_CLIENT_FLAG = envJson.getString("REQUEST_CLIENT_FLAG");
        if (headers != null) {
            requestHost = headers.getString("host");
            requestClientId = headers.getString(REQUEST_CLIENT_FLAG);
        }
        var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getNextId();
        var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
        var msg255 = logicLog.getMsg() == null ? null : logicLog.getMsg().substring(0, Math.min(logicLog.getMsg().length(), 255));
        LogicLogEntity logEntity = new LogicLogEntity()
                .setSuccess(logicLog.isSuccess())
                .setMessage(msg255)
                .setBizId(logicLog.getBizId())
                .setVersion(logicLog.getVersion())
                .setItemLogs(JSONArray.toJSONString(logicLog.getItemLogs()))
                .setReturnData(logicLog.getReturnDataStr() != null ? logicLog.getReturnDataStr() : null)
                .setLogicId(logicLog.getLogicId())
                .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                .setNextId(nextId)
                .setNextName(nextName)
                .setIsOver(logicLog.isOver())
                .setEnv(RuntimeUtil.getEnvObject().getNODE_ENV())
                .setHost(requestHost)
                .setClientId(requestClientId);
        logMapper.insert(logEntity);
    }

    public List<LogicLogEntity> queryLogs(String logicId) {
        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("logicId", logicId);
        wrapper.orderByDesc("id");
        return logMapper.selectList(wrapper);
    }

    public List<LogicLogEntity> queryBizLogs(String logicId, String bizId) {
        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("logicId", logicId);
        wrapper.eq("bizId", bizId);
        wrapper.orderByDesc("serverTime");
        return logMapper.selectList(wrapper);
    }

    public int deleteLog(long aid) {
        return logMapper.deleteById(aid);
    }
}
