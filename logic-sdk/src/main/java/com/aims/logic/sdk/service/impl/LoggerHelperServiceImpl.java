package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.custom.CustomDiscardOldestPolicy;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.sdk.config.LogicLogServiceConfig;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class LoggerHelperServiceImpl implements LoggerHelperService {
    private final LogicInstanceService instanceService;
    private final LogicLogService logicLogService;
    private final JdbcTemplate jdbcTemplate;
    private LogicLogServiceConfig logicLogServiceConfig;
    static RejectedExecutionHandler customDiscardOldestPolicy = new CustomDiscardOldestPolicy();
    /**
     * 新增执行日志logic_log日志
     *
     * @param logicLog
     */
    private final static ExecutorService logExecutor = new ThreadPoolExecutor(
            2,
            4,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            customDiscardOldestPolicy
    );

    @Autowired
    public LoggerHelperServiceImpl(
            LogicInstanceService _instanceService,
            LogicLogService _logicLogService,
            JdbcTemplate _jdbcTemplate,
            LogicLogServiceConfig _logicLogServiceConfig) {
        this.instanceService = _instanceService;
        this.logicLogService = _logicLogService;
        this.jdbcTemplate = _jdbcTemplate;
        this.logicLogServiceConfig = _logicLogServiceConfig;
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

    public void startBizRunning(LogicLog logicLog) {
        String env = RuntimeUtil.getEnvObject().getNODE_ENV();
        var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
        var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
        Map<String, Object> valueMaps = new HashMap<>();
        valueMaps.put("message", null);
        valueMaps.put("paramsJson", logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString());
        valueMaps.put("varsJson", logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString());
        valueMaps.put("varsJsonEnd", logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString());
        valueMaps.put("isRunning", logicLog.isRunning());
        valueMaps.put("startTime", logicLog.getStartTime());
        valueMaps.put("stopTime", null);
        valueMaps.put("duration", -1);
        valueMaps.put("nextId", nextId);
        valueMaps.put("isAsync", logicLog.getIsAsync());
        valueMaps.put("nextName", nextName);
        valueMaps.put("env", env);
        instanceService.updateById(logicLog.getInstanceId(), valueMaps);
        try {
            instanceService.triggerBeforeLogicRun(logicLog);
        } catch (Exception e) {
            log.error("beforeLogicRun回调异常", e);
            e.printStackTrace();
        }

    }

    public void stopBizRunning(LogicLog logicLog) {
        // triggerEventListener(logicLog);
        logicLog.setIsRunning(false);
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("isRunning", false);
        valuesMap.put("stopTime", logicLog.getStopTime());
        valuesMap.put("duration", logicLog.getDuration());
        valuesMap.put("success", logicLog.isSuccess());
        var msg255 = logicLog.getMsg() == null ? null
                : logicLog.getMsg().length() > 255 ? logicLog.getMsg().substring(0, 255) : logicLog.getMsg();
        valuesMap.put("message", msg255);
        instanceService.updateById(logicLog.getInstanceId(), valuesMap);
        instanceService.triggerAfterLogicStop(logicLog);
        if (logicLog.isOver()) {
            instanceService.triggerBizCompleted(logicLog);
        }

    }

    /**
     * 新增或更新运行实例日志
     *
     * @param logicLog
     */

    public void addOrUpdateInstance(LogicLog logicLog) {
        if (logicLog.getInstanceId() != null) {
            updateInstance(logicLog);
        } else {
            addInstance(logicLog);
        }
    }

    public void updateInstance(LogicLog logicLog) {
        String env = RuntimeUtil.getEnvObject().getNODE_ENV();
        var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
        var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
        var msg255 = logicLog.getMsg() == null ? null
                : logicLog.getMsg().length() > 255 ? logicLog.getMsg().substring(0, 255) : logicLog.getMsg();
        Map<String, Object> valueMaps = new HashMap<>();
        valueMaps.put("success", logicLog.isSuccess());
        valueMaps.put("message", msg255);
        // valueMaps.put("returnData",
        // logicLog.getReturnDataStr());停用，加快更新速度，可以在logic_log表查看返回值
        valueMaps.put("paramsJson", logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString());
        valueMaps.put("varsJson", logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString());
        valueMaps.put("varsJsonEnd",
                logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString());
        valueMaps.put("isOver", logicLog.isOver());
        valueMaps.put("isRunning", logicLog.isRunning());
        valueMaps.put("startTime", logicLog.getStartTime());
        valueMaps.put("stopTime", logicLog.getStopTime());
        valueMaps.put("duration", logicLog.getDurationUntilNow());
        valueMaps.put("nextId", nextId);
        valueMaps.put("nextName", nextName);
        valueMaps.put("env", env);
        instanceService.updateById(logicLog.getInstanceId(), valueMaps);
    }

    public String addInstance(LogicLog logicLog) {
        String env = RuntimeUtil.getEnvObject().getNODE_ENV();
        var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
        var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
        var msg255 = logicLog.getMsg() == null ? null
                : logicLog.getMsg().length() > 255 ? logicLog.getMsg().substring(0, 255) : logicLog.getMsg();
        LogicInstanceEntity newIns = new LogicInstanceEntity()
                .setSuccess(logicLog.isSuccess())
                .setMessage(msg255)
                .setMessageId(logicLog.getMsgId())
                .setBizId(logicLog.getBizId())
                .setIsRunning(logicLog.isRunning())
                .setVersion(logicLog.getVersion())
                .setReturnData(logicLog.getReturnDataStr())
                .setLogicId(logicLog.getLogicId())
                .setParentLogicId(logicLog.getParentLogicId())
                .setParentBizId(logicLog.getParentBizId())
                .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                .setEnvsJson(logicLog.getEnvsJson() == null ? null : logicLog.getEnvsJson().toJSONString())
                .setIsOver(logicLog.isOver())
                .setIsRunning(logicLog.isRunning())
                .setStartTime(logicLog.getStartTime())
                .setNextId(nextId)
                .setNextName(nextName)
                .setEnv(env);
        var newInsId = instanceService.insertAndGetId(newIns);
        logicLog.setInstanceId(newInsId);
        return newInsId;
    }

    public void addLogicLog(LogicLog logicLog) {
        // 使用线程池异步执行日志记录
        logExecutor.execute(() -> {
            execAddLogicLog(logicLog);
        });
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    // 将日志插入数据库
    public void execAddLogicLog(LogicLog logicLog) {
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
            var msg255 = logicLog.getMsg() == null ? null
                    : logicLog.getMsg().substring(0, Math.min(logicLog.getMsg().length(), 255));
            LogicLogEntity logEntity = new LogicLogEntity()
                    .setSuccess(logicLog.isSuccess())
                    .setMessage(msg255)
                    .setMessageId(logicLog.getMsgId())
                    .setServerTime(logicLog.getStartTime())
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setItemLogs(JSONArray.toJSONString(logicLog.getItemLogs()
                            .subList(Math.max(logicLog.getItemLogs().size() - 30, 0),
                                    logicLog.getItemLogs().size())))
                    .setReturnData(logicLog.getReturnDataStr())
                    .setLogicId(logicLog.getLogicId())
                    .setParamsJson(
                            logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .setVarsJsonEnd(
                            logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .setNextId(nextId)
                    .setNextName(nextName)
                    .setIsOver(logicLog.isOver())
                    .setEnv(RuntimeUtil.getEnvObject().getNODE_ENV())
                    .setHost(requestHost)
                    .setClientId(requestClientId);
            if (logicLog.getId() != null)
                logEntity.setId(logicLog.getId().toString());
            logicLogService.insert(logEntity);
            log.info("[{}]bizId:{},[{}]日志添加成功，点击回放：{}/logic/index.html#/debug/logic-log/i/{}", logicLog.getLogicId(), logicLog.getBizId(), logicLogServiceConfig.logStoreType, RuntimeUtil.getOnlineHost(), logicLog.getId());
        } catch (Exception e) {
            log.error("添加logicLog日志异常:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    // 将日志插入es
    public void addLogicLogToEs(LogicLog logicLog) {

    }
    // public List<LogicLogEntity> queryLogs(String logicId) {
    // QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
    // wrapper.eq("logicId", logicId);
    // wrapper.orderByDesc("id");
    // return logMapper.selectList(wrapper);
    // }

    // public List<LogicLogEntity> queryBizLogs(String logicId, String bizId) {
    // QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
    // wrapper.eq("logicId", logicId);
    // wrapper.eq("bizId", bizId);
    // wrapper.orderByDesc("serverTime");
    // return logMapper.selectList(wrapper);
    // }

    public void clearLog() {
        jdbcTemplate.update("truncate logic_log");
    }
}
