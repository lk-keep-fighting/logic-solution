package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.aims.logic.util.JsonUtil;
import com.alibaba.fastjson2.JSONObject;

public class LogicRuntimeSdk {

    /**
     * 传入逻辑配置与入参执行逻辑
     *
     * @param logicConfig 逻辑配置
     * @param pars        入参
     * @return 返回参数
     */
    public static LogicRunResult run(JSONObject logicConfig, JSONObject pars) {
        return run(logicConfig, pars, null);
    }

    /**
     * 传入逻辑配置、入参、自定义环境变量执行逻辑
     *
     * @param logicConfig 逻辑配置
     * @param pars        入参
     * @param customEnv   自定义环境变量
     * @return 返回参数
     */
    public static LogicRunResult run(JSONObject logicConfig, JSONObject pars, JSONObject customEnv) {
        JSONObject env = RuntimeUtil.readEnv();
        env = JsonUtil.jsonMerge(customEnv, env);
        return new com.aims.logic.runtime.logic.LogicRunner(logicConfig, env).run(pars);
    }

    /**
     * 传入逻辑编号与入参执行逻辑
     *
     * @param logicId 逻辑编号
     * @param pars    入参
     * @return 返回参数
     */
    public static LogicRunResult run(String logicId, JSONObject pars) {
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
    public static LogicRunResult run(String logicId, JSONObject pars, JSONObject customEnv) {
        JSONObject config = RuntimeUtil.readLogicConfig(logicId);
        JSONObject env = RuntimeUtil.readEnv();
        env = JsonUtil.jsonMerge(customEnv, env);
        return new com.aims.logic.runtime.logic.LogicRunner(config, env).run(pars);
    }
}
