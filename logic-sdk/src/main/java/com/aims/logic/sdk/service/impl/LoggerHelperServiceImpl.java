package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LoggerHelperService;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.service.LogicLogService;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LoggerHelperServiceImpl implements LoggerHelperService {
    private final LogicInstanceService instanceService;
    private final LogicLogService logicLogService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LoggerHelperServiceImpl(
            LogicInstanceService _instanceService,
            LogicLogService _logicLogService,
            JdbcTemplate _jdbcTemplate
    ) {
        this.instanceService = _instanceService;
        this.logicLogService = _logicLogService;
        this.jdbcTemplate = _jdbcTemplate;
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
        addLogicLog(logicLog);
    }

    public void updateInstanceStatus(String instanceId, boolean success, String msg) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("success", success);
        var msg255 = msg == null ? null : msg.length() > 255 ? msg.substring(0, 255) : msg;
        valuesMap.put("message", msg255);
        instanceService.updateById(instanceId, valuesMap);
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
        var msg255 = logicLog.getMsg() == null ? null : logicLog.getMsg().length() > 255 ? logicLog.getMsg().substring(0, 255) : logicLog.getMsg();
        if (logicLog.getInstanceId() != null) {
            Map<String, Object> valueMaps = new HashMap<>();
            valueMaps.put("success", logicLog.isSuccess());
            valueMaps.put("message", msg255);
            valueMaps.put("returnData", logicLog.getReturnDataStr());
            valueMaps.put("paramsJson", logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString());
            valueMaps.put("varsJson", logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString());
            valueMaps.put("varsJsonEnd", logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString());
            valueMaps.put("isOver", logicLog.isOver());
            valueMaps.put("nextId", nextId);
            valueMaps.put("nextName", nextName);
            valueMaps.put("env", env);
            instanceService.updateById(logicLog.getInstanceId(), valueMaps);
        } else {
            LogicInstanceEntity newIns = new LogicInstanceEntity()
                    .setSuccess(logicLog.isSuccess())
                    .setMessage(msg255)
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setReturnData(logicLog.getReturnDataStr())
                    .setLogicId(logicLog.getLogicId())
                    .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .setEnvsJson(logicLog.getEnvsJson() == null ? null : logicLog.getEnvsJson().toJSONString())
                    .setIsOver(logicLog.isOver())
                    .setNextId(nextId)
                    .setNextName(nextName)
                    .setEnv(env);
            instanceService.insert(newIns);
            logicLog.setInstanceId(newIns.getId());
        }
    }

    /**
     * 新增执行日志logic_log日志
     *
     * @param logicLog
     */
    public void addLogicLog(LogicLog logicLog) {
        try {
            if (logicLog.isLogOff()) {
                log.info("LogicId:{}，关闭了日志，无法回放业务实例", logicLog.getLogicId());
                return;
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
            var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getNextId();
            var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
            var msg255 = logicLog.getMsg() == null ? null : logicLog.getMsg().substring(0, Math.min(logicLog.getMsg().length(), 255));
            LogicLogEntity logEntity = new LogicLogEntity()
                    .setSuccess(logicLog.isSuccess())
                    .setMessage(msg255)
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setItemLogs(JSONArray.toJSONString(logicLog.getItemLogs()))
                    .setReturnData(logicLog.getReturnDataStr())
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
            logicLogService.insert(logEntity);
        } catch (Exception e) {
            log.error("添加logicLog日志异常:{}", e.getMessage());
            e.printStackTrace();
        }

    }

//    public List<LogicLogEntity> queryLogs(String logicId) {
//        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
//        wrapper.eq("logicId", logicId);
//        wrapper.orderByDesc("id");
//        return logMapper.selectList(wrapper);
//    }

//    public List<LogicLogEntity> queryBizLogs(String logicId, String bizId) {
//        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
//        wrapper.eq("logicId", logicId);
//        wrapper.eq("bizId", bizId);
//        wrapper.orderByDesc("serverTime");
//        return logMapper.selectList(wrapper);
//    }

    public void clearLog() {
        jdbcTemplate.update("truncate logic_log");
    }
}
