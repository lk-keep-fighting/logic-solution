package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.dto.RunnerStatusEnum;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import com.aims.logic.runtime.contract.enums.LogicItemType;
import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.env.LogicAppConfig;
import com.aims.logic.runtime.env.LogicEnvObject;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.LogicRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.aims.logic.runtime.util.JsonUtil;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.runtime.util.StringConcurrencyUtil;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LoggerHelperService;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.util.TransactionalUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author liukun
 */
@Slf4j
@Service
public class LogicRunnerServiceImpl implements LogicRunnerService {
    private final LoggerHelperService logService;
    private final LogicInstanceService insService;
    private final LogicConfigStoreService configStoreService;
    private final LogicAppConfig appConfig;
    private final TransactionalUtils transactionalUtils;
    /**
     * 私有环境变量，若为null则使用全局环境变量
     */
    private LogicEnvObject envObject = null;
    private JSONObject envJson = null;

    @Autowired
    public LogicRunnerServiceImpl(LoggerHelperService logService,
                                  LogicInstanceService insService,
                                  LogicConfigStoreService _configStoreService,
                                  TransactionalUtils transactionalUtils,
                                  LogicAppConfig appConfig) {
        this.logService = logService;
        this.insService = insService;
        this.configStoreService = _configStoreService;
        this.appConfig = appConfig;
        this.transactionalUtils = transactionalUtils;
        RuntimeUtil.AppConfig = appConfig;
        RuntimeUtil.logicConfigStoreService = configStoreService;
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
        this.envJson = JsonUtil.jsonMerge(customEnv, RuntimeUtil.getEnvJson());
        this.envObject = RuntimeUtil.toEnvObject(envJson);
        if (isOverride) {
            RuntimeUtil.setEnv(this.envJson.clone());
        }
        return this.envJson;
    }

    @Override
    public JSONObject getEnvJson() {
        if (this.envJson == null)
            return RuntimeUtil.getEnvJson();
        else return this.envJson;
    }

    @Override
    public LogicEnvObject getEnv() {
        if (this.envObject == null)
            return RuntimeUtil.getEnvObject();
        return this.envObject;
    }

    @Override
    public LogicRunnerService newInstance(JSONObject env) {
        var ins = new LogicRunnerServiceImpl(logService, insService, configStoreService, transactionalUtils, appConfig);
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
        logService.addLogicLog(res.getLogicLog());
//        logService.addOrUpdateInstanceAndAddLogicLog(res.getLogicLog());
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
        String lockKey = logicId + "-" + bizId;
        try {
            StringConcurrencyUtil.lock(lockKey);
            log.info("[{}]bizId:{}-get lock key:{}", logicId, bizId, lockKey);
            return runBizInstance(logicId, bizId, parsMap);
        } catch (Exception e) {
            log.error("[{}]bizId:{}-runBizByMap catch逻辑异常:{}", logicId, bizId, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            StringConcurrencyUtil.unlock(lockKey);
            log.info("[{}]bizId:{}-unlock key:{}", logicId, bizId, lockKey);
        }
    }
//        synchronized (lockKey.intern()) {
//            return runBizWithTransaction(logicId, bizId, parsMap);
//        }

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
    public LogicRunResult runBizByVerifyCode(String logicId, String bizId, String
            verifyCode, Map<String, Object>
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
     * 清除日志 truncate logic_log
     */
    @Override
    public void clearLog() {
        logService.clearLog();
    }

    @Override
    public void clearCompletedInstance() {
        insService.deleteCompletedBizInstance();
    }

    /**
     * 加事务执行业务逻辑，每执行一个节点提交一次
     *
     * @param logicId
     * @param bizId
     * @param parsMap
     * @return
     */
    LogicRunResult runBizInstance(String logicId, String bizId, Map<String, Object> parsMap) {
        JSONObject cacheVarsJson = null;
        String startId = null;
        String logicVersion = null;
        String instanceId = null;
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
            if (insEntity != null) {
                cacheVarsJson = JSON.isValid(insEntity.getVarsJsonEnd()) ? JSON.parseObject(insEntity.getVarsJsonEnd()) : null;
                startId = insEntity.getNextId();
                logicVersion = insEntity.getVersion();
                instanceId = insEntity.getId().toString();
            }
            if (insEntity != null && insEntity.getIsOver()) {
                log.error("指定的logicId:{},bizId:{}已完成，无法重复执行", logicId, bizId);
                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
            }
        }
        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
        if (config == null) {
            log.error("未发现指定的逻辑logicId:{},bizId:{}未执行", logicId, bizId);
            return new LogicRunResult().setSuccess(false).setMsg("未发现指定的逻辑：" + logicId);
        }
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson(), parsMap, cacheVarsJson, startId, bizId);

        LogicItemTreeNode nextItem = runner.getStartNode();
        String startNodeType = nextItem.getType();
//        if (LogicItemType.waitForContinue.compareType(startNodeType) || LogicItemType.start.compareType(startNodeType)) {//起始为交互点，判断交互点配置的事务范围
        var tranScope = nextItem.getTranScope();
        if (tranScope == null || tranScope.equals(LogicItemTransactionScope.def))
            tranScope = RuntimeUtil.getEnvObject().getDefaultTranScope();
        log.info("[{}]bizId:{},tranScope:{}", logicId, bizId, tranScope);
        switch (tranScope) {
            case everyRequest:
                log.info("[{}]bizId:{},runBizInstanceWithEveryRequestTran", logicId, bizId);
                return runBizInstanceWithEveryRequestTran(instanceId, logicId, bizId, runner, nextItem);
            case off:
                log.info("[{}]bizId:{},runBizInstanceWithoutTran", logicId, bizId);
                return runBizInstanceWithoutTran(instanceId, logicId, bizId, runner, nextItem);
            case everyNode2:
                log.info("[{}]bizId:{},runBizInstanceWithEveryNodeTran2", logicId, bizId);
                return runBizInstanceWithEveryNodeTran2(instanceId, logicId, bizId, runner, nextItem);
            case everyJavaNode:
            case everyNode:
            default:
                break;
        }
//        }
        log.info("[{}]bizId:{},runBizInstanceWithEveryNodeTran", logicId, bizId);
        return runBizInstanceWithEveryNodeTran(instanceId, logicId, bizId, runner, nextItem);
    }

    private TransactionStatus beginNewTranIfNewGroup(TransactionStatus lastTran, FunctionContext ctx, LogicItemTreeNode curItem) {
        if (ctx.getCurTranGroupId() == null || ctx.getLastTranGroupId() == null) {
            log.info("[{}]bizId:{}-当前节点：{}-{},begin开启事务组：{}", ctx.getLogicId(), ctx.getBizId(), curItem.getType(), curItem.getName(), ctx.getCurTranGroupId());
            if (lastTran != null) {
                if (lastTran.isNewTransaction()) {
                    return lastTran;
                } else if (!lastTran.isCompleted()) {
                    transactionalUtils.commit(lastTran);
                }
            }
            return transactionalUtils.newTran();
        }
        if (!ctx.getCurTranGroupId().equals(ctx.getLastTranGroupId())) {
            log.info("[{}]bizId:{}-当前节点：{}-{},begin开启事务组：{}", ctx.getLogicId(), ctx.getBizId(), curItem.getType(), curItem.getName(), ctx.getCurTranGroupId());
            if (lastTran != null) {
                if (lastTran.isNewTransaction()) {
                    return lastTran;
                } else if (!lastTran.isCompleted()) {
                    transactionalUtils.commit(lastTran);
                }
            }
            return transactionalUtils.newTran();
        }
        log.info("[{}]bizId:{}-当前节点：{}-{},begin复用事务组：{}", ctx.getLogicId(), ctx.getBizId(), curItem.getType(), curItem.getName(), ctx.getLastTranGroupId());
        return lastTran;
    }

    private void commitCurTranIfNextIsNewGroup(TransactionStatus curTran, FunctionContext ctx, LogicItemTreeNode curItem) {
        if (ctx.getCurTranGroupId() == null || ctx.getNextTranGroupId() == null) {
            log.info("[{}]bizId:{}-当前节点：{}-{},commit 提交事务组：{}", ctx.getLogicId(), ctx.getBizId(), curItem.getType(), curItem.getName(), ctx.getCurTranGroupId());
            if (!curTran.isCompleted()) {
                transactionalUtils.commit(curTran);
            } else {
                log.info("[{}]bizId:{},commit 未执行，isCompleted=true", ctx.getLogicId(), ctx.getBizId());
            }
            return;
        }
        if (!ctx.getCurTranGroupId().equals(ctx.getNextTranGroupId())) {
            log.info("[{}]bizId:{}-当前节点：{}-{},commit 提交事务组：{}", ctx.getLogicId(), ctx.getBizId(), curItem.getType(), curItem.getName(), ctx.getCurTranGroupId());
            if (!curTran.isCompleted()) {
                transactionalUtils.commit(curTran);
            } else {
                log.info("[{}]bizId:{},commit 未执行，isCompleted=true", ctx.getLogicId(), ctx.getBizId());
            }
            return;
        }
        log.info("[{}]bizId:{}-当前节点：{}-{},commit 未提交，保留当前事务组：{}", ctx.getLogicId(), ctx.getBizId(), curItem.getType(), curItem.getName(), ctx.getLastTranGroupId());
    }

    /***
     * 事务范围为每个节点
     * @param instanceId
     * @param logicId
     * @param bizId
     * @param runner
     * @param nextItem
     * @return
     */

    private LogicRunResult runBizInstanceWithEveryNodeTran(String instanceId, String logicId, String bizId, LogicRunner
            runner, LogicItemTreeNode nextItem) {
        log.info("[{}]bizId:{}-runItemWithEveryJavaNodeTran-insId:{}", logicId, bizId, instanceId);
        LogicLog logicLog = LogicLog.newBizLogBeforeRun(instanceId, runner.getFnCtx(), nextItem);
        TransactionStatus curTranStatus = null;
        LogicItemRunResult itemRes = null;
        var ctx = runner.getFnCtx();
        LogicItemTreeNode curItem;
        while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            try {
                curItem = nextItem;
                ctx.setCurTranGroupId(Functions.runJsExpressByContext(runner.getFnCtx(), curItem.getTranGroupId()));
                curTranStatus = beginNewTranIfNewGroup(curTranStatus, runner.getFnCtx(), curItem);
                ctx.setLastTranGroupId(ctx.getCurTranGroupId());
                itemRes = runner.runItem(nextItem);
                log.info("[{}]bizId:{}-当前节点：{}-{}，执行结果,success:{},msg:{}", logicId, bizId, nextItem.getType(), nextItem.getName(), itemRes.isSuccess(), itemRes.getMsg());
                nextItem = runner.findNextItem(nextItem);
                ctx.setNextTranGroupId(Functions.runJsExpressByContext(runner.getFnCtx(), nextItem == null ? null : nextItem.getTranGroupId()));
                logicLog.addItemLog(itemRes);
                runner.refreshStatus(itemRes.isSuccess(), nextItem);
                logicLog.setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.getRunnerStatus() == RunnerStatusEnum.End)
                        .setNextItem(nextItem)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                if (itemRes.isSuccess()) {
                    logService.addOrUpdateInstance(logicLog);
                    log.info("[{}]bizId:{},begin commit in runItemWithEveryJavaNodeTran-itemResIsSuccess=true", logicId, bizId);
                    commitCurTranIfNextIsNewGroup(curTranStatus, runner.getFnCtx(), curItem);
                } else {
                    log.info("[{}]bizId:{},节点执行失败，begin rollback，success=false,msg:{}, in runItemWithEveryJavaNodeTran", logicId, bizId, itemRes.getMsg());
                    transactionalUtils.rollback(curTranStatus);
                    log.info("[{}]bizId:{},节点执行失败，rollback ok，事务组:{}", logicId, bizId, ctx.getCurTranGroupId());
                    logService.updateInstanceStatus(logicLog.getInstanceId(), false, itemRes.getMsg());
                    logService.addLogicLog(logicLog);
                    return LogicRunResult.fromLogicLog(logicLog);
                }
            } catch (Exception e) {
                var msg = e.toString();
                log.error("[{}]bizId:{},节点执行catch到意外的异常：{},begin rollback", logicId, bizId, msg);
                e.printStackTrace();
                if (!curTranStatus.isCompleted()) {
                    transactionalUtils.rollback(curTranStatus);
                    log.info("[{}]bizId:{},catch意外异常，rollback ok", logicId, bizId);
                } else {
                    log.info("[{}]bizId:{},catch意外异常，rollback 未执行，isCompleted=true", logicId, bizId);
                }
                logicLog.setSuccess(false).setMsg(msg);
                logService.addLogicLog(logicLog);
                logService.updateInstanceStatus(logicLog.getInstanceId(), false, msg);
                return LogicRunResult.fromLogicLog(logicLog);
            }
        }
        logService.addLogicLog(logicLog);
        return LogicRunResult.fromLogicLog(logicLog);
    }

    private LogicRunResult runBizInstanceWithEveryNodeTran2(String instanceId, String logicId, String bizId, LogicRunner
            runner, LogicItemTreeNode nextItem) {
        log.info("[{}]bizId:{}-runItemWithEveryJavaNodeTran2-insId:{}", logicId, bizId, instanceId);
        LogicLog logicLog = LogicLog.newBizLogBeforeRun(instanceId, runner.getFnCtx(), nextItem);
        TransactionStatus curTranStatus = null;
        LogicItemRunResult itemRes = null;
        var ctx = runner.getFnCtx();
        LogicItemTreeNode curItem;
        while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            try {
                curItem = nextItem;
                ctx.setCurTranGroupId(Functions.runJsExpressByContext(runner.getFnCtx(), curItem.getTranGroupId()));
                curTranStatus = beginNewTranIfNewGroup(curTranStatus, runner.getFnCtx(), curItem);
                ctx.setLastTranGroupId(ctx.getCurTranGroupId());
                itemRes = runner.runItem(nextItem);
                log.info("[{}]bizId:{}-当前节点：{}-{}，执行结果,success:{},msg:{}", logicId, bizId, nextItem.getType(), nextItem.getName(), itemRes.isSuccess(), itemRes.getMsg());
                nextItem = runner.findNextItem(nextItem);
                ctx.setNextTranGroupId(Functions.runJsExpressByContext(runner.getFnCtx(), nextItem == null ? null : nextItem.getTranGroupId()));
                logicLog.addItemLog(itemRes);
                runner.refreshStatus(true, nextItem);
                logicLog.setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.getRunnerStatus() == RunnerStatusEnum.End)
                        .setNextItem(nextItem)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                if (itemRes.isSuccess()) {
                    logService.addOrUpdateInstance(logicLog);
                    log.info("[{}]bizId:{},begin commit in runItemWithEveryJavaNodeTran-itemResIsSuccess=true", logicId, bizId);
                    commitCurTranIfNextIsNewGroup(curTranStatus, runner.getFnCtx(), curItem);
                } else {
                    log.info("[{}]bizId:{},节点执行失败，begin rollback，success=false,msg:{}, in runItemWithEveryJavaNodeTran", logicId, bizId, itemRes.getMsg());
                    transactionalUtils.rollback(curTranStatus);
                    log.info("[{}]bizId:{},节点执行失败，rollback ok，事务组:{}", logicId, bizId, ctx.getCurTranGroupId());
                    logService.updateInstanceStatus(logicLog.getInstanceId(), false, itemRes.getMsg());
                    logService.addLogicLog(logicLog);
                }
            } catch (Exception e) {
                var msg = e.toString();
                log.error("[{}]bizId:{},节点执行catch到意外的异常：{},begin rollback", logicId, bizId, msg);
                e.printStackTrace();
                if (!curTranStatus.isCompleted()) {
                    transactionalUtils.rollback(curTranStatus);
                    log.info("[{}]bizId:{},catch意外异常，rollback ok", logicId, bizId);
                } else {
                    log.info("[{}]bizId:{},catch意外异常，rollback 未执行，isCompleted=true", logicId, bizId);
                }
                logicLog.setSuccess(false).setMsg(msg);
                logService.addLogicLog(logicLog);
                logService.updateInstanceStatus(logicLog.getInstanceId(), false, msg);
                return LogicRunResult.fromLogicLog(logicLog);
            }
        }
        logService.addLogicLog(logicLog);
        return LogicRunResult.fromLogicLog(logicLog);
    }

    /**
     * 事务范围为每个交互点
     *
     * @param logicId
     * @param bizId
     * @param runner
     * @param nextItem
     * @return
     */
    private LogicRunResult runBizInstanceWithEveryRequestTran(String instanceId, String logicId, String bizId, LogicRunner
            runner, LogicItemTreeNode nextItem) {
        LogicLog logicLog = LogicLog.newBizLogBeforeRun(instanceId, runner.getFnCtx(), nextItem);
        LogicItemRunResult itemRes = null;
        if (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
            TransactionStatus begin = null;
            begin = transactionalUtils.newTran();
            while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
                try {
                    itemRes = runner.runItem(nextItem);
                    nextItem = runner.findNextItem(nextItem);
                    logicLog.addItemLog(itemRes);
                    if (itemRes.isSuccess()) {
                        runner.refreshStatus(true, nextItem);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    log.error("[{}]bizId:{}节点执行catch到意外的异常：{},跳出while", logicId, bizId, e.getMessage());
                    itemRes.setSuccess(false);
                    itemRes.setMsg(e.getMessage());
                    break;
                }
            }

            //本次交互完成，没有错误则提交，否则本次交互全部回滚，只更新实例success状态和消息
            if (itemRes.isSuccess()) {
                logicLog.setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.refreshStatus(itemRes.isSuccess(), nextItem) == RunnerStatusEnum.End)
                        .setNextItem(nextItem)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                logService.addOrUpdateInstanceAndAddLogicLog(logicLog);
                transactionalUtils.commit(begin);
            } else {
                if (!begin.isCompleted()) {
                    log.info("[{}]bizId:{},节点执行失败，begin rollback", logicId, bizId);
                    transactionalUtils.rollback(begin);
                } else {
                    log.info("[{}]bizId:{},节点执行失败，事务isCompleted=true，rollback 未执行", logicId, bizId);
                }
                logicLog.setSuccess(false);
                logService.updateInstanceStatus(logicLog.getInstanceId(), false, itemRes.getMsg());
                logService.addLogicLog(logicLog);
                return new LogicRunResult().setLogicLog(logicLog)
                        .setSuccess(false)
                        .setMsg(itemRes.getMsg());
            }
        }
        return new LogicRunResult()
                .setLogicLog(logicLog)
                .setData(itemRes == null ? null : itemRes.getData())
                .setSuccess(itemRes == null ? true : itemRes.isSuccess())
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
    private LogicRunResult runBizInstanceWithoutTran(String instanceId, String logicId, String bizId, LogicRunner runner, LogicItemTreeNode
            nextItem) {
        LogicLog logicLog = LogicLog.newBizLogBeforeRun(instanceId, runner.getFnCtx(), nextItem);
        LogicItemRunResult itemRes = null;
        if (LogicItemType.start.compareType(nextItem.getType())) {//如果为开始节点，业务实例先入库，记录本次请求，避免后续失败数据丢失
            itemRes = runner.runItem(nextItem);
            nextItem = runner.findNextItem(nextItem);
            logicLog.addItemLog(itemRes);
            logicLog.setNextItem(nextItem);
            logService.addOrUpdateInstance(logicLog);
            runner.refreshStatus(true, nextItem);
        }
        try {
            while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
                itemRes = runner.runItem(nextItem);
                nextItem = runner.findNextItem(nextItem);
                logicLog.addItemLog(itemRes);
                if (itemRes.isSuccess()) {
                    runner.refreshStatus(true, nextItem);
                } else {
                    runner.refreshStatus(false, nextItem);
                    logicLog.setOver(false)
                            .setSuccess(false).setMsg(itemRes.getMsg());
                }
            }
        } catch (Exception e) {
            logicLog.setOver(false)
                    .setSuccess(false).setMsg(e.getMessage());
        }
        logicLog.setVarsJson_end(runner.getFnCtx().get_var());
        if (logicLog.isSuccess())
            logicLog.setOver(runner.refreshStatus(itemRes.isSuccess(), nextItem) == RunnerStatusEnum.End)
                    .setNextItem(nextItem).setMsg(itemRes.getMsg());
        if (!runner.getFnCtx().isLogOff())
            logService.addLogicLog(logicLog);
        logService.addOrUpdateInstance(logicLog);
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
        JSONObject parsJson = null;
        if (bizId != null && !bizId.isBlank()) {
            LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
            if (insEntity != null) {
                if (insEntity.getSuccess()) {
                    return new LogicRunResult().setSuccess(false).
                            setMsg(String.format("业务[%s]没有发生异常，不可重试！", bizId));
                }
                parsJson = JSON.isValid(insEntity.getParamsJson()) ? JSON.parseObject(insEntity.getParamsJson()) : null;
            } else {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("业务逻辑[%s]的实例[%s]不存在，不可重试！", logicId, bizId));
            }
            if (insEntity != null && insEntity.getIsOver()) {
                return new LogicRunResult().setSuccess(false).setMsg(String.format("指定的bizId:%s已完成执行，无法重复执行。", bizId));
            }
        }
        return runBizInstance(logicId, bizId, parsJson);
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
                return insService.updateById(insEntity.getId(), insEntity) > 0;
            }
        }
        return false;
    }

    public int deleteBizInstance(String logicId, String bizId) {
        LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
        if (insEntity != null) {
            return insService.removeById(insEntity.getId());
        }
        log.info("要删除的业务实例logicId:{},bizId:{}不存在！", logicId, bizId);
        return 0;
    }

    public int deleteCompletedBizInstanceByLogicId(String logicId) {
        return insService.deleteCompletedBizInstanceByLogicId(logicId);
    }
}
