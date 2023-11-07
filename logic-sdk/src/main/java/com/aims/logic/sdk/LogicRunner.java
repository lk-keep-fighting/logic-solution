package com.aims.logic.sdk;

import com.aims.logic.contract.dto.LogicRunResult;
import com.aims.logic.sdk.service.impl.LoggerServiceImpl;
import com.aims.logic.util.JsonUtil;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liukun
 */
@Service
public class LogicRunner {
    private final LoggerServiceImpl logService;

    @Autowired
    public LogicRunner(LoggerServiceImpl logService) {
        this.logService = logService;
    }

    public LogicRunResult run(String logicId, String parsJsonString) {
        JSONObject pars = parsJsonString == null ? null : JSONObject.parseObject(parsJsonString);
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
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env).run(pars);
        logService.addLog(res);
        return res;
    }

}
