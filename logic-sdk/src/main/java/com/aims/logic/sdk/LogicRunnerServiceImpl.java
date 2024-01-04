package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.dto.RunnerStatusEnum;
import com.aims.logic.runtime.contract.logger.LogicItemLog;
import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.env.LogicAppConfig;
import com.aims.logic.runtime.runner.LogicRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.runtime.util.SpringContextUtil;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.service.impl.LoggerServiceImpl;
import com.aims.logic.sdk.util.TransactionalUtils;
import com.aims.logic.runtime.util.JsonUtil;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author liukun
 */
@Service
public class LogicRunnerServiceImpl implements LogicRunnerService {
    private final LoggerServiceImpl logService;
    private final LogicInstanceService insService;
    private final LogicAppConfig appConfig;
    private final TransactionalUtils transactionalUtils;

    /**
     * 环境变量，初始化时加载一次，内部方法共享
     */
    JSONObject env;

    @Autowired
    public LogicRunnerServiceImpl(LoggerServiceImpl logService,
                                  LogicInstanceService insService,
                                  TransactionalUtils transactionalUtils,
                                  LogicAppConfig appConfig) {
        this.logService = logService;
        this.insService = insService;
        this.appConfig = appConfig;
        this.transactionalUtils = transactionalUtils;
        RuntimeUtil.AppConfig = appConfig;
        env = RuntimeUtil.readEnv();
    }

    public void init() {
        env = RuntimeUtil.readEnv();
    }


    /**
     * 设置环境变量
     *
     * @param customEnv  自定义环境变量
     * @param isOverride 是否覆盖默认环境变量
     * @return
     */
    @Override
    public JSONObject setEnv(JSONObject customEnv, boolean isOverride) {
        if (isOverride) {
            this.env = customEnv.clone();
        } else {
            var defEnv = RuntimeUtil.readEnv();
            this.env = JsonUtil.jsonMerge(defEnv, customEnv);
        }
        return this.env;
    }

    @Override
    public LogicRunnerService newInstance(JSONObject env) {
        var ins = new LogicRunnerServiceImpl(logService, insService, transactionalUtils, appConfig);
        ins.setEnv(env, true);
        return ins;
    }

    @Override
    public LogicRunResult runByJson(String logicId, String parsJsonString) {
        JSONObject pars = parsJsonString == null ? null : JSONObject.parseObject(parsJsonString);
        return runByMap(logicId, pars);
    }

    @Override
    public LogicRunResult runByObjectArgs(String logicId, Object... pars) {
        Map<String, Object> parsMap = new HashMap<>();
        for (int i = 0; i < pars.length; i++) {
            parsMap.put("_p" + (i + 1), pars[i]);
        }
        return runByMap(logicId, parsMap);
    }

    /**
     * 传入逻辑编号、入参、自定义环境变量执行逻辑
     *
     * @param logicId 逻辑编号
     * @param parsMap 入参
     * @return 返回参数
     */
    @Override
    public LogicRunResult runByMap(String logicId, Map<String, Object> parsMap) {
        JSONObject config = RuntimeUtil.readLogicConfig(logicId);
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env).run(parsMap);
        logService.addOrUpdateInstanceLog(res.getLogicLog());
        return res;
    }


    /**
     * 通过业务标识运行,若执行报错，下一次从报错的交互点重新执行
     *
     * @param logicId        逻辑编号
     * @param bizId          业务唯一标识
     * @param parsJsonString 入参json字符串
     * @return 执行结果
     */
    @Override
    public LogicRunResult runBizByJson(String logicId, String bizId, String parsJsonString) {
        JSONObject pars = parsJsonString == null ? null : JSONObject.parseObject(parsJsonString);
        return runBizByMap(logicId, bizId, pars);
    }

    @Override
    public LogicRunResult runBizByObjectArgs(String logicId, String bizId, Object... pars) {
        Map<String, Object> parsMap = new HashMap<>();
        for (int i = 0; i < pars.length; i++) {
            parsMap.put("_p" + (i + 1), pars[i]);
        }
        return runBizByMap(logicId, bizId, parsMap);
    }

    /**
     * 执行业务逻辑
     *
     * @param logicId 逻辑编号
     * @param bizId   业务编号
     * @param parsMap 入参Map,key为参数名，value为实参
     * @return 执行结果
     */
    @Override
    public LogicRunResult runBizByMap(String logicId, String bizId, Map<String, Object> parsMap) {
        return runBizWithTransaction(logicId, bizId, parsMap);
//        JSONObject env = RuntimeUtil.getEnvJson();
//        env = JsonUtil.jsonMerge(customEnv, env);
//        String cacheVarsJson = null;
//        String startId = null;
//        String logicVersion = null;
//        if (bizId != null && !bizId.isBlank()) {
//            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
//            if (insEntity != null) {
//                cacheVarsJson = insEntity.getVarsJsonEnd();
//                startId = insEntity.getNextId();
//                logicVersion = insEntity.getVersion();
//            }
//            if (insEntity != null && insEntity.getIsOver()) {
//                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
//            }
//        }
//        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
//        if (config == null) {
//            throw new RuntimeException("未发现指定的逻辑：" + logicId);
//        }
//        var res = new com.aims.logic.runtime.runner.LogicRunner(config, env, bizId)
//                .run(startId, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
//        res.getLogicLog().setBizId(bizId);
//        logService.addOrUpdateInstanceLog(res.getLogicLog());
//        return res;
    }

    /***
     * 执行前先校验交互点是否正确
     * @param logicId
     * @param bizId
     * @param verifyCode
     * @param parsJsonString
     * @return
     */
    @Override
    public LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, String parsJsonString) {
        JSONObject pars = parsJsonString == null ? null : JSONObject.parseObject(parsJsonString);
        return runBizByVerifyCode(logicId, bizId, verifyCode, pars);
    }

    void verifyCode(LogicInstanceEntity insEntity, LogicRunner runner, String verifyCode) {
        var itemNode = runner.findItemByCode(verifyCode);
        if (itemNode == null) throw new RuntimeException("未找到指定的交互点编号：" + verifyCode);
        var startNode = runner.getStartItem(insEntity.getNextId());
        if (!Objects.equals(itemNode.getId(), startNode.getId())) {
            throw new RuntimeException(String.format("非法交互，请求执行【%s】，与待交互点【%s】不一致。", itemNode.getName(), startNode.getName()));
        }
    }

    @Override
    public LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, Map<String, Object>
            parsMap) {
        if (bizId == null || bizId.isBlank()) throw new RuntimeException("未指定业务标识！");
        if (verifyCode == null || verifyCode.isBlank())
            throw new RuntimeException("未指定期望执行节点！");
        LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
        if (insEntity != null && insEntity.getIsOver()) {
            return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
        }
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
        verifyCode(insEntity, runner, verifyCode);
        var res = runner
                .run(startId, parsMap, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addOrUpdateInstanceLog(res.getLogicLog());
        return res;
    }


    LogicRunResult runBizWithTransaction(String logicId, String bizId, Map<String, Object> parsMap) {
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
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, env, parsMap, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null, startId, bizId);

        LogicLog logicLog = new LogicLog();
        logicLog.setBizId(bizId)
                .setVarsJson_end(runner.getFnCtx().get_var())
                .setEnvsJson(runner.getLogicLog().getEnvsJson())
                .setLogicId(logicId)
                .setVersion(runner.getLogic().getVersion());
        TransactionStatus begin = null;
        LogicItemRunResult itemRes = null;
        LogicItemTreeNode nextItem = runner.getStartNode();
        List<LogicItemLog> itemLogs = new ArrayList<>();
        while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            try {
                begin = transactionalUtils.begin();
                itemRes = runner.runItem(nextItem);
                nextItem = runner.findNextItem(nextItem);
                itemLogs.clear();
                itemLogs.add(itemRes.getItemLog());
                logicLog.setItemLogs(itemLogs)
                        .setOver(runner.updateStatus(itemRes, nextItem) == RunnerStatusEnum.End)
                        .setNextItem(runner.getFnCtx().getNextItem())
                        .setMsg(itemRes.getMsg())
                        .setSuccess(itemRes.isSuccess());
                logService.addOrUpdateInstanceLog(logicLog);
                if (itemRes.isSuccess()) {
                    transactionalUtils.commit(begin);
                } else {
                    transactionalUtils.rollback(begin);
                    logService.addLogicRunLog(logicLog);
                    return new LogicRunResult().setLogicLog(logicLog)
                            .setSuccess(false)
                            .setMsg(itemRes.getMsg());
                }
                runner.updateStatus(itemRes, nextItem);
            } catch (Exception e) {
//                transactionalUtils.rollback(begin);
                logService.addLogicRunLog(logicLog);
                return new LogicRunResult().setLogicLog(logicLog)
                        .setSuccess(false)
                        .setMsg(e.getMessage());
            }
        }
//        while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
//            try {
//                begin = transactionalUtils.begin();
//                itemRes = runner.runItem(nextItem);
//                nextItem = runner.findNextItem(nextItem);
//                itemLogs = new ArrayList<>();
//                itemLogs.add(itemRes.getItemLog());
//                logicLog.setItemLogs(itemLogs)
//                        .setReturnDataStr(itemRes.getDataString())
//                        .setOver(runner.getRunnerStatus() == RunnerStatusEnum.End)
//                        .setNextItem(nextItem)
//                        .setMsg(itemRes.getMsg())
//                        .setSuccess(itemRes.isSuccess());
//                logService.addOrUpdateInstanceLog(logicLog);
//                transactionalUtils.commit(begin);
//                runner.updateStatus(itemRes, nextItem);
//            } catch (Exception e) {
//                transactionalUtils.rollback(begin);
//                logService.addLogicRunLog(logicLog);
//                return new LogicRunResult().setLogicLog(logicLog)
//                        .setSuccess(false)
//                        .setMsg(e.getMessage());
//            }
//        }
//
        return new LogicRunResult().setLogicLog(logicLog)
                .setData(itemRes.getData())
                .setSuccess(itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    public LogicRunResult runBizToNextJavaMethod(String logicId, String bizId, JSONObject pars) {
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
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, env, pars, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null, startId, bizId);

        LogicItemRunResult itemRes = null;
        LogicItemTreeNode curItem = runner.getStartNode();
        LogicItemTreeNode nextItem = null;
        LogicLog logicLog = new LogicLog();
        List<LogicItemLog> itemLogs = new ArrayList<>();
        try {

            itemRes = runner.runItem(runner.getStartNode());
            nextItem = runner.findNextItem(runner.getStartNode());
            itemLogs.add(itemRes.getItemLog());
            logicLog.setBizId(bizId)
                    .setVarsJson_end(runner.getFnCtx().get_var())
                    .setEnvsJson(runner.getLogicLog().getEnvsJson())
                    .setLogicId(logicId)
                    .setItemLogs(itemLogs)
                    .setOver(runner.updateStatus(itemRes, nextItem) == RunnerStatusEnum.End)
                    .setNextItem(runner.getFnCtx().getNextItem())
                    .setVersion(runner.getLogic().getVersion())
                    .setMsg(itemRes.getMsg())
                    .setSuccess(itemRes.isSuccess());
            while (!"java".equals(curItem.getType()) && runner.updateStatus(itemRes, nextItem) == RunnerStatusEnum.Continue) {
                try {
                    itemRes = runner.runItem(nextItem);
                    nextItem = runner.findNextItem(nextItem);
                    itemLogs = new ArrayList<>();
                    itemLogs.add(itemRes.getItemLog());
                    logicLog.setItemLogs(itemLogs)
                            .setReturnDataStr(itemRes.getDataString())
                            .setOver(runner.getRunnerStatus() == RunnerStatusEnum.End)
                            .setNextItem(nextItem)
                            .setMsg(itemRes.getMsg())
                            .setSuccess(itemRes.isSuccess());
                } catch (Exception e) {
                    return new LogicRunResult().setLogicLog(logicLog)
                            .setSuccess(false)
                            .setMsg(e.getMessage());
                }
            }
        } catch (Exception e) {
            return new LogicRunResult().setLogicLog(logicLog)
                    .setSuccess(false)
                    .setMsg(e.getMessage());
        }
        return new LogicRunResult().setLogicLog(logicLog)
                .setData(itemRes.getData())
                .setSuccess(itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    public void updateBizAfterRunBizToNextJavaMethod(LogicRunResult result) {
        logService.addOrUpdateInstanceLog(result.getLogicLog());
    }
}
