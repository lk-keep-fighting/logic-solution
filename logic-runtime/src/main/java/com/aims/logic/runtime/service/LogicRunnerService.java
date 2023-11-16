package com.aims.logic.runtime.service;

import com.aims.logic.contract.dto.LogicRunResult;
import com.alibaba.fastjson2.JSONObject;

public interface LogicRunnerService {
    LogicRunResult run(String logicId, String parsJsonString);

    LogicRunResult run(String logicId, JSONObject pars, JSONObject customEnv);

    LogicRunResult runBiz(String logicId, String bizId, String parsJsonString);

    LogicRunResult runBiz(String logicId, String bizId, JSONObject pars, JSONObject customEnv);

    LogicRunResult runBizByCode(String logicId, String bizId, String itemCode, String parsJsonString);

    LogicRunResult runBizByCode(String logicId, String bizId, String itemCode, JSONObject pars, JSONObject customEnv);
}
