package com.aims.logic.runtime.service;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

public interface LogicRunnerService {
    LogicRunResult run(String logicId, String parsJsonString);

    LogicRunResult run(String logicId,  Map<String, Object> parsMap, JSONObject customEnv);

    LogicRunResult runBiz(String logicId, String bizId, String parsJsonString);

    /**
     * 根据bizId保持交互点状态，每次请求继续往下执行
     * 失败则下次请求开始的交互点不变
     *
     * @param logicId
     * @param bizId
     * @param parsMap
     * @param customEnv
     * @return
     */

    LogicRunResult runBiz(String logicId, String bizId, Map<String, Object> parsMap, JSONObject customEnv);

    /**
     * 与runBiz的区别是，当节点报错时，会将当前报错节点记录为下一次请求的执行节点
     *
     * @param logicId
     * @param bizId
     * @param pars
     * @param customEnv
     * @return
     */
    LogicRunResult runBizStepByStep(String logicId, String bizId, JSONObject pars, JSONObject customEnv);

    LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, String parsJsonString);

    /**
     * 根据verifyCode判断是否与当前等待的交互点相同，不一致则报错
     *
     * @param logicId
     * @param bizId
     * @param verifyCode
     * @param pars
     * @param customEnv
     * @return
     */
    LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, JSONObject pars, JSONObject customEnv);

    LogicRunResult runBizWithTransaction(String logicId, String bizId, Map<String, Object> parsMap, JSONObject customEnv);
}
