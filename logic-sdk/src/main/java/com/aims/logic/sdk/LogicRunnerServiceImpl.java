package com.aims.logic.sdk;

import com.aims.logic.contract.dto.LogicRunResult;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.service.impl.LoggerServiceImpl;
import com.aims.logic.util.JsonUtil;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author liukun
 */
@Service
public class LogicRunnerServiceImpl implements LogicRunnerService {
    private final LoggerServiceImpl logService;
    private final LogicInstanceService insService;

    @Autowired
    public LogicRunnerServiceImpl(LoggerServiceImpl logService,
                                  LogicInstanceService insService) {
        this.logService = logService;
        this.insService = insService;
    }

    @Override
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
    @Override
    public LogicRunResult run(String logicId, JSONObject pars, JSONObject customEnv) {
        JSONObject config = RuntimeUtil.readLogicConfig(logicId);
        JSONObject env = RuntimeUtil.readEnv();
        env = JsonUtil.jsonMerge(customEnv, env);
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env).run(pars);
        logService.addOrUpdateInstanceLog(res);
        return res;
    }


    /**
     * 通过业务标识运行
     *
     * @param logicId        逻辑编号
     * @param bizId          业务唯一标识
     * @param parsJsonString 入参json字符串
     * @return 执行结果
     */
    @Override
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
    @Override
    public LogicRunResult runBiz(String logicId, String bizId, JSONObject pars, JSONObject customEnv) {
        JSONObject env = RuntimeUtil.getEnvJson();
        env = JsonUtil.jsonMerge(customEnv, env);
        String cacheVarsJson = null;
        String startId = null;
        String logicVersion = null;
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
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
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env, bizId)
                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addOrUpdateInstanceLog(res);
        return res;
    }

    @Override
    public LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, String parsJsonString) {
        JSONObject pars = parsJsonString == null ? null : JSONObject.parseObject(parsJsonString);
        return runBizByVerifyCode(logicId, bizId, verifyCode, pars, null);
    }

    @Override
    public LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, JSONObject pars, JSONObject customEnv) {
        if (bizId == null || bizId.isBlank()) throw new RuntimeException("未指定业务标识！");
        if (verifyCode == null || verifyCode.isBlank())
            throw new RuntimeException("未指定期望执行节点！");
        LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
        if (insEntity != null && insEntity.getIsOver()) {
            return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
        }
        JSONObject env = RuntimeUtil.getEnvJson();
        env = JsonUtil.jsonMerge(customEnv, env);
        String cacheVarsJson = null;
        String startId = null;
        String logicVersion = null;
        if (insEntity != null) {
            cacheVarsJson = insEntity.getVarsJsonEnd();
            startId = insEntity.getNextId();
            logicVersion = insEntity.getVersion();
        }
        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
        if (config == null) {
            throw new RuntimeException("未发现指定的逻辑：" + logicId);
        }
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, env, bizId);
        var itemNode = runner.findItemByCode(verifyCode);
        if (itemNode == null) throw new RuntimeException("未找到指定的交互点编号：" + verifyCode);
        var startNode = runner.getStartItem(startId);
        if (!Objects.equals(itemNode.getId(), startNode.getId())) {
            throw new RuntimeException(String.format("非法交互，请求执行【%s】，与待交互点【%s】不一致。", itemNode.getName(), startNode.getName()));
        }
        var res = runner
                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addOrUpdateInstanceLog(res);
        return res;
    }

    @Override
    public LogicRunResult runBizStepByStep(String logicId, String bizId, JSONObject pars, JSONObject customEnv) {
        JSONObject env = RuntimeUtil.getEnvJson();
        env = JsonUtil.jsonMerge(customEnv, env);
        String cacheVarsJson = null;
        String startId = null;
        String logicVersion = null;
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
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
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env, bizId)
                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        var logicLog = res.getLogicLog();
        if (!logicLog.isSuccess()) {//发生错误，当前模式为步进模式，把当前节点设置为下一次请求的执行节点
            var itemLogs = logicLog.getItemLogs();
            var curItem = itemLogs.get(itemLogs.size() - 1);
            res.getLogicLog().setNextItem(curItem.getConfig());
        }
        logService.addOrUpdateInstanceLog(res);
        return res;
    }

}
