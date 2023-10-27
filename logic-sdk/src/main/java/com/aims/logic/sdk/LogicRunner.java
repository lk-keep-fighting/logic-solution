package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.sdk.service.LogicLogService;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.aims.logic.util.JsonUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogicRunner {
    private final LogicLogService logService;

    @Autowired
    public LogicRunner(LogicLogService _logService) {
        this.logService = _logService;
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
        var lastedLog = logService.findLastBizLog(logicId, bizId);
        var cacheVarsJson = lastedLog == null ? null : lastedLog.getVarsJsonEnd();
        var startId = lastedLog == null ? null : lastedLog.getNextId();
        if (lastedLog != null && lastedLog.isOver()) {
            return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
        }
        var res = new com.aims.logic.runtime.logic.LogicRunner(config, env)
                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addLog(res);
        return res;
    }
}
