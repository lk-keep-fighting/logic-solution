package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.dto.RunnerStatusEnum;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import com.aims.logic.runtime.contract.enums.LogicItemType;
import com.aims.logic.runtime.contract.logger.LogicItemLog;
import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.env.LogicAppConfig;
import com.aims.logic.runtime.env.LogicAppEnvObject;
import com.aims.logic.runtime.runner.LogicRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.service.impl.LoggerServiceImpl;
import com.aims.logic.sdk.util.TransactionalUtils;
import com.aims.logic.runtime.util.JsonUtil;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

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
        RuntimeUtil.initEnv();
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
            RuntimeUtil.setEnv(customEnv.clone());
        } else {
            var defEnv = RuntimeUtil.readEnv();
            RuntimeUtil.setEnv(JsonUtil.jsonMerge(defEnv, customEnv));
        }
        return RuntimeUtil.getEnvJson();
    }

    @Override
    public JSONObject getEnvJson() {
        return RuntimeUtil.getEnvJson();
    }

    @Override
    public LogicAppEnvObject getEnv() {
        return RuntimeUtil.getEnvObject();
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
        var res = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson()).run(parsMap);
        logService.addOrUpdateInstanceAndAddLogicLog(res.getLogicLog());
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
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson(), bizId);
        verifyCode(insEntity, runner, verifyCode);
        var res = runner
                .run(startId, parsMap, JSON.isValid(cacheVarsJson) ? JSON.parseObject(cacheVarsJson) : null);
        res.getLogicLog().setBizId(bizId);
        logService.addOrUpdateInstanceAndAddLogicLog(res.getLogicLog());
        return res;
    }


    /**
     * 加事务执行业务逻辑，每执行一个节点提交一次
     *
     * @param logicId
     * @param bizId
     * @param parsMap
     * @return
     */
    LogicRunResult runBizWithTransaction(String logicId, String bizId, Map<String, Object> parsMap) {
        JSONObject cacheVarsJson = null;
        String startId = null;
        String logicVersion = null;
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
            if (insEntity != null) {
                cacheVarsJson = JSON.isValid(insEntity.getVarsJsonEnd()) ? JSON.parseObject(insEntity.getVarsJsonEnd()) : null;
                startId = insEntity.getNextId();
                logicVersion = insEntity.getVersion();
            }
            if (insEntity != null && insEntity.getIsOver()) {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
            }
        }
        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
        if (config == null) {
            return new LogicRunResult().setSuccess(false).setMsg("未发现指定的逻辑：" + logicId);
        }
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson(), parsMap, cacheVarsJson, startId, bizId);

        LogicItemTreeNode nextItem = runner.getStartNode();
        String startNodeType = nextItem.getType();
        if (LogicItemType.waitForContinue.compareType(startNodeType) || LogicItemType.start.compareType(startNodeType)) {//起始为交互点，判断交互点配置的事务范围
            var tranScope = nextItem.getTranScope();
            if (tranScope == null) tranScope = LogicItemTransactionScope.on;
            switch (tranScope) {
                case everyRequest:
                    return runItemWithEveryRequestTran(logicId, bizId, runner, nextItem);
                case off:
                    return runItemWithNoTran(logicId, bizId, runner, nextItem);
                case on:
                case everyJavaNode:
                default:
                    break;
            }
        }
        return runItemWithEveryJavaNodeTran(logicId, bizId, runner, nextItem);
    }

    /**
     * 每个java节点事务
     *
     * @param logicId
     * @param bizId
     * @param runner
     * @param nextItem
     * @return
     */

    private LogicRunResult runItemWithEveryJavaNodeTran(String logicId, String bizId, LogicRunner runner, LogicItemTreeNode nextItem) {
        LogicLog logicLog = new LogicLog();
        logicLog.setBizId(bizId).setLogicId(logicId).setVersion(runner.getLogic().getVersion())
                .setParamsJson(JSONObject.from(runner.getFnCtx().get_par())).setVarsJson(runner.getFnCtx().get_var()).setEnvsJson(runner.getFnCtx().get_env())
                .setNextItem(nextItem);

        TransactionStatus begin = null;
        LogicItemRunResult itemRes = null;
        List<LogicItemLog> itemLogs = new ArrayList<>();
        while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            try {
                begin = transactionalUtils.begin();
                itemRes = runner.runItem(nextItem);
                nextItem = runner.findNextItem(nextItem);
                itemLogs.clear();
                itemLogs.add(itemRes.getItemLog());
                logicLog.setItemLogs(itemLogs).setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.refreshStatus(itemRes.isSuccess(), nextItem) == RunnerStatusEnum.End)
                        .setNextItem(nextItem)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
                if (itemRes.isSuccess()) {
                    transactionalUtils.commit(begin);
                } else {
                    transactionalUtils.rollback(begin);
                    logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                    logService.addLogicLog(logicLog);
                    return new LogicRunResult().setLogicLog(logicLog)
                            .setSuccess(false)
                            .setMsg(itemRes.getMsg());
                }
                runner.refreshStatus(itemRes.isSuccess(), nextItem);
            } catch (Exception e) {
//                transactionalUtils.rollback(begin);
                logService.addLogicLog(logicLog);
                logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                return new LogicRunResult().setLogicLog(logicLog)
                        .setSuccess(false)
                        .setMsg(e.getMessage());
            }
        }
        return new LogicRunResult().setLogicLog(logicLog)
                .setData(itemRes.getData())
                .setSuccess(itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    /**
     * 每个交互点事务
     *
     * @param logicId
     * @param bizId
     * @param runner
     * @param nextItem
     * @return
     */
    private LogicRunResult runItemWithEveryRequestTran(String logicId, String bizId, LogicRunner runner, LogicItemTreeNode nextItem) {
        LogicLog logicLog = new LogicLog();
        logicLog.setBizId(bizId).setLogicId(logicId).setVersion(runner.getLogic().getVersion())
                .setParamsJson(JSONObject.from(runner.getFnCtx().get_par())).setVarsJson(runner.getFnCtx().get_var()).setEnvsJson(runner.getFnCtx().get_env())
                .setNextItem(nextItem);
        if (LogicItemType.start.compareType(nextItem.getType())) {//如果为开始节点，业务实例先入库，记录本次请求，避免后续失败数据丢失
            nextItem = runner.findNextItem(nextItem);
            logicLog.setNextItem(nextItem).setVarsJson_end(runner.getFnCtx().get_var());
            logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
            runner.refreshStatus(true, nextItem);
        }
        LogicItemRunResult itemRes = null;
        List<LogicItemLog> itemLogs = new ArrayList<>();
        if (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            TransactionStatus begin = null;
            begin = transactionalUtils.begin();
            while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
                try {
                    itemRes = runner.runItem(nextItem);
                    nextItem = runner.findNextItem(nextItem);
                    itemLogs.clear();
                    itemLogs.add(itemRes.getItemLog());
                    logicLog.setItemLogs(itemLogs).setVarsJson_end(runner.getFnCtx().get_var())
                            .setOver(runner.refreshStatus(itemRes.isSuccess(), nextItem) == RunnerStatusEnum.End)
                            .setNextItem(nextItem)
                            .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                    logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
                    if (itemRes.isSuccess()) {
                        runner.refreshStatus(true, nextItem);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    logService.addLogicLog(logicLog);
                    logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                    return new LogicRunResult().setLogicLog(logicLog)
                            .setSuccess(false)
                            .setMsg(e.getMessage());
                }
            }
            //本次交互完成，没有错误则提交，否则本次交互全部回滚，只更新实例success状态和消息
            if (itemRes.isSuccess())
                transactionalUtils.commit(begin);
            else {
                transactionalUtils.rollback(begin);
                logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                logService.addLogicLog(logicLog);
                return new LogicRunResult().setLogicLog(logicLog)
                        .setSuccess(false)
                        .setMsg(itemRes.getMsg());
            }
        }
        return new LogicRunResult().setLogicLog(logicLog)
                .setData(itemRes.getData())
                .setSuccess(itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    /**
     * 无事务
     *
     * @param logicId
     * @param bizId
     * @param runner
     * @param nextItem
     * @return
     */
    private LogicRunResult runItemWithNoTran(String logicId, String bizId, LogicRunner runner, LogicItemTreeNode nextItem) {
        LogicLog logicLog = new LogicLog();
        logicLog.setBizId(bizId).setLogicId(logicId).setVersion(runner.getLogic().getVersion())
                .setParamsJson(JSONObject.from(runner.getFnCtx().get_par())).setVarsJson(runner.getFnCtx().get_var()).setEnvsJson(runner.getFnCtx().get_env())
                .setNextItem(nextItem);
        if (LogicItemType.start.compareType(nextItem.getType())) {//如果为开始节点，业务实例先入库，记录本次请求，避免后续失败数据丢失
            nextItem = runner.findNextItem(nextItem);
            logicLog.setNextItem(nextItem).setVarsJson_end(runner.getFnCtx().get_var());
            logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
            runner.refreshStatus(true, nextItem);
        }
        LogicItemRunResult itemRes = null;
        List<LogicItemLog> itemLogs = new ArrayList<>();
        if (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
                try {
                    itemRes = runner.runItem(nextItem);
                    nextItem = runner.findNextItem(nextItem);
                    itemLogs.clear();
                    itemLogs.add(itemRes.getItemLog());
                    logicLog.setItemLogs(itemLogs).setVarsJson_end(runner.getFnCtx().get_var())
                            .setOver(runner.refreshStatus(itemRes.isSuccess(), nextItem) == RunnerStatusEnum.End)
                            .setNextItem(nextItem)
                            .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                    logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
                    if (itemRes.isSuccess()) {
                        runner.refreshStatus(true, nextItem);
                    } else {
                        return new LogicRunResult().setLogicLog(logicLog)
                                .setSuccess(false)
                                .setMsg(itemRes.getMsg());
                    }
                } catch (Exception e) {
                    logService.addLogicLog(logicLog);
                    logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                    return new LogicRunResult().setLogicLog(logicLog)
                            .setSuccess(false)
                            .setMsg(e.getMessage());
                }
            }
        }
        return new LogicRunResult().setLogicLog(logicLog)
                .setData(itemRes.getData())
                .setSuccess(itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    /**
     * 通过实例记录重试业务，从记录的待执行节点继续执行；
     * 从实例读取缓存的入参与临时变量
     *
     * @param logicId
     * @param bizId
     * @return
     */
    public LogicRunResult retryErrorBiz(String logicId, String bizId) {
        JSONObject cacheVarsJson = null;
        JSONObject parsJson = null;
        JSONObject cacheEnvsJson = null;
        String startId = null;
        String logicVersion = null;
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
            if (insEntity != null) {
                if (insEntity.getSuccess()) {
                    return new LogicRunResult().setSuccess(false).
                            setMsg(String.format("业务[%s]没有发生异常，不可重试！", bizId));
                }
                //上次未执行成功，发生错误的节点不会更新实例
                cacheVarsJson = JSON.isValid(insEntity.getVarsJsonEnd()) ? JSON.parseObject(insEntity.getVarsJsonEnd()) : null;
                parsJson = JSON.isValid(insEntity.getParamsJson()) ? JSON.parseObject(insEntity.getParamsJson()) : null;
                cacheEnvsJson = JSON.isValid(insEntity.getEnvsJson()) ? JSON.parseObject(insEntity.getEnvsJson()) : null;
                startId = insEntity.getNextId();
                logicVersion = insEntity.getVersion();
            } else {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("业务逻辑[%s]的实例[%s]不存在，不可重试！", logicId, bizId));
            }
            if (insEntity != null && insEntity.getIsOver()) {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
            }
        }
        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
        if (config == null) {
            return new LogicRunResult().setSuccess(false).setMsg(String.format("未发现逻辑配置文件：[%s]！", logicId));
        }
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, cacheEnvsJson, parsJson, cacheVarsJson, startId, bizId);

        LogicLog logicLog = new LogicLog();
        logicLog.setBizId(bizId)
                .setParamsJson(JSONObject.from(runner.getFnCtx().get_par()))
                .setVarsJson(runner.getFnCtx().get_var())
                .setEnvsJson(runner.getFnCtx().get_env())
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
                        .setOver(runner.refreshStatus(itemRes.isSuccess(), nextItem) == RunnerStatusEnum.End)
                        .setVarsJson_end(runner.getFnCtx().get_var())
                        .setNextItem(runner.getFnCtx().getNextItem())
                        .setMsg(itemRes.getMsg())
                        .setSuccess(itemRes.isSuccess());
                logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
                if (itemRes.isSuccess()) {
                    transactionalUtils.commit(begin);
                } else {
                    transactionalUtils.rollback(begin);
                    logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                    logService.addLogicLog(logicLog);
                    return new LogicRunResult().setLogicLog(logicLog)
                            .setSuccess(false)
                            .setMsg(itemRes.getMsg());
                }
                runner.refreshStatus(itemRes.isSuccess(), nextItem);
            } catch (Exception e) {
//                transactionalUtils.rollback(begin);
                logService.updateInstanceStatus(logicId, bizId, false, itemRes.getMsg());
                logService.addLogicLog(logicLog);

                return new LogicRunResult().setLogicLog(logicLog)
                        .setSuccess(false)
                        .setMsg(e.getMessage());
            }
        }
        return new LogicRunResult().setLogicLog(logicLog)
                .setData(itemRes.getData())
                .setSuccess(itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    /**
     * 更新当前业务实例入参
     *
     * @param logicId
     * @param bizId
     * @param pars
     * @return
     */
    @Override
    public boolean updateBizInstanceParams(String logicId, String bizId, Object... pars) {
        Map<String, Object> parsMap = new HashMap<>();
        for (int i = 0; i < pars.length; i++) {
            parsMap.put("_p" + (i + 1), pars[i]);
        }
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
            if (insEntity != null) {
                insEntity.setParamsJson(JSONObject.toJSONString(parsMap));
                return insService.updateById(insEntity);
            }
        }
        return false;
    }
}
