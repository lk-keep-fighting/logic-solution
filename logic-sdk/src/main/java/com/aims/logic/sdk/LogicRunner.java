package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LoggerService;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.aims.logic.util.JsonUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LogicRunner {
    private final LoggerService logService;
    private final LogicInstanceService insService;

    @Autowired
    public LogicRunner(LoggerService _logService,
                       LogicInstanceService _insService) {
        this.logService = _logService;
        this.insService = _insService;
    }

    /**
     * 传入逻辑配置与入参执行逻辑
     *
     * @param logicConfig 逻辑配置
     * @param pars        入参
     * @return 返回参数
     */
//    public LogicRunResult run(JSONObject logicConfig, JSONObject pars) {
//        return run(logicConfig, pars, null);
//    }

    /**
     * 传入逻辑配置、入参、自定义环境变量执行逻辑
     *
     * @param logicConfig 逻辑配置
     * @param pars        入参
     * @param customEnv   自定义环境变量
     * @return 返回参数
     */
//    public LogicRunResult run(JSONObject logicConfig, JSONObject pars, JSONObject customEnv) {
//        JSONObject env = RuntimeUtil.readEnv();
//        env = JsonUtil.jsonMerge(customEnv, env);
//        var res = new com.aims.logic.runtime.logic.LogicRunner(logicConfig, env).run(pars);
//        logService.addLog(res);
//        return res;
//    }

    /**
     * 传入逻辑编号与入参执行逻辑
     *
     * @param logicId 逻辑编号
     * @param pars    入参
     * @return 返回参数
     */
    public LogicRunResult run(String logicId, JSONObject pars) {
        return run(logicId, pars, null);
    }

    /**
     * 传入逻辑编号、入参、自定义环境变量执行逻辑
     *
     * @param logicId   逻辑编号
     * @param pars      入参
     * @param customEnv 自定义环境变量
     * @return 返回参数
     */
    public LogicRunResult run(String logicId, JSONObject pars, JSONObject customEnv) {
        JSONObject config = RuntimeUtil.readLogicConfig(logicId);
        JSONObject env = RuntimeUtil.readEnv();
        env = JsonUtil.jsonMerge(customEnv, env);
        var res = new com.aims.logic.runtime.logic.LogicRunner(config, env).run(pars);
        logService.addLog(res);
        return res;
    }

    public LogicRunResult runBiz(String logicId, String bizId, JSONObject pars) {
        return runBiz(logicId, bizId, pars, null);
    }

    public LogicRunResult runBiz(String logicId, String bizId, JSONObject pars, JSONObject customEnv) {
        JSONObject config = RuntimeUtil.readLogicConfig(logicId);
        if (config == null) throw new RuntimeException("未发现指定的逻辑：" + logicId);
        config.put("id", logicId);//自动修复文件名编号与内部配置编号不同的问题
        JSONObject env = RuntimeUtil.getEnvJson();
        env = JsonUtil.jsonMerge(customEnv, env);
        String startId = null;
        String cacheVarsJson = null;
        if (bizId != null && !bizId.isBlank()) {
            QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
            q.allEq(Map.of("logicId", logicId, "bizId", bizId));
            LogicInstanceEntity insEntity = insService.getOne(q);
            cacheVarsJson = insEntity == null ? null : insEntity.getVarsJsonEnd();
            startId = insEntity == null ? null : insEntity.getNextId();
            if (insEntity != null && insEntity.isOver()) {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
            }
        }
        var res = new com.aims.logic.runtime.logic.LogicRunner(config, env)
                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addLog(res);
        return res;
    }
}
