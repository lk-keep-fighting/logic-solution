package com.aims.logic.sdk;

import com.aims.logic.contract.dto.LogicRunResult;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.impl.LoggerServiceImpl;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.util.JsonUtil;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BizLogicRunner {
    private final LoggerServiceImpl logService;
    private final LogicInstanceService insService;

    @Autowired
    public BizLogicRunner(LoggerServiceImpl _logService,
                          LogicInstanceService _insService) {
        this.logService = _logService;
        this.insService = _insService;
    }

    /**
     * 通过业务标识运行
     *
     * @param logicId        逻辑编号
     * @param bizId          业务唯一标识
     * @param parsJsonString 入参json字符串
     * @return 执行结果
     */
    public LogicRunResult runBiz(String logicId, String bizId, String parsJsonString) {
        JSONObject pars = parsJsonString == null ? null : JSONObject.parseObject(parsJsonString);
        return runBiz(logicId, bizId, pars, null);
    }

    /**
     * 执行业务逻辑
     *
     * @param logicId   逻辑编号
     * @param bizId     业务编号
     * @param pars      入参json
     * @param customEnv 环境变量json
     * @return 执行结果
     */
    public LogicRunResult runBiz(String logicId, String bizId, JSONObject pars, JSONObject customEnv) {
        JSONObject env = RuntimeUtil.getEnvJson();
        env = JsonUtil.jsonMerge(customEnv, env);
        String cacheVarsJson = null;
        String startId = null;
        String logicVersion = null;
        if (bizId != null && !bizId.isBlank()) {
            QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
            q.allEq(Map.of("logicId", logicId, "bizId", bizId));
            LogicInstanceEntity insEntity = insService.getOne(q);
            if (insEntity != null) {
                cacheVarsJson = insEntity.getVarsJsonEnd();
                startId = insEntity.getNextId();
                logicVersion = insEntity.getVersion();
            }
            if (insEntity != null && insEntity.getIsOver()) {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
            }
        }
        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
        if (config == null) {
            throw new RuntimeException("未发现指定的逻辑：" + logicId);
        }
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env)
                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addLog(res);
        return res;
    }
}
