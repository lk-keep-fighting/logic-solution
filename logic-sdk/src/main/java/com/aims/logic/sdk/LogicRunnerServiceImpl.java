package com.aims.logic.sdk;

import com.aims.logic.runtime.LogicBizException;
import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.*;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import com.aims.logic.runtime.contract.enums.LogicItemType;
import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.env.LogicAppConfig;
import com.aims.logic.runtime.env.LogicSysEnvDto;
import com.aims.logic.runtime.exception.BizManuallyStoppedException;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.LogicRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.aims.logic.runtime.util.IdWorker;
import com.aims.logic.runtime.util.JsonUtil;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LoggerHelperService;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.util.TransactionalUtils;
import com.aims.logic.sdk.util.lock.BizLock;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
//    private LogicSysEnvDto envObject = null;
    private JSONObject envJson = null;
    /**
     * 父逻辑编号
     */
    private String parentLogicId = null;
    private boolean isAsync = false;
    /**
     * 父业务标识
     */
    private String parentBizId = null;

    static IdWorker idWorker = new IdWorker(2, 1);
    BizLock bizLock;

    @Autowired
    public LogicRunnerServiceImpl(LoggerHelperService logService,
                                  LogicInstanceService insService,
                                  LogicConfigStoreService _configStoreService,
                                  TransactionalUtils transactionalUtils,
                                  LogicAppConfig appConfig,
                                  BizLock bizLock) {
        this.logService = logService;
        this.insService = insService;
        this.configStoreService = _configStoreService;
        this.appConfig = appConfig;
        this.transactionalUtils = transactionalUtils;
        this.bizLock = bizLock;
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
        if (isOverride) this.envJson = customEnv;
        else
            this.envJson = JsonUtil.jsonMerge(RuntimeUtil.getEnvJson(), customEnv);
//        this.envObject = RuntimeUtil.toEnvObject(envJson);
//        if (isOverride) {
//            RuntimeUtil.setEnv(this.envJson.clone());
//        }
        return this.envJson;
    }

    @Override
    public JSONObject getEnvJson() {
        if (this.envJson == null)
            return RuntimeUtil.getEnvJson();
        else return this.envJson;
    }

    @Override
    public LogicSysEnvDto getEnv() {
        return RuntimeUtil.toEnvObject(this.envJson);
    }

//    @Override
//    public LogicSysEnvDto getEnv() {
//        if (this.envObject == null)
//            return RuntimeUtil.getEnvObject();
//        return this.envObject;
//    }

    @Override
    public LogicRunnerService newInstance(JSONObject env) {
        return newInstance(env, null, null, false);
    }

    @Override
    public LogicRunnerService newInstance(JSONObject env, String parentLogicId, String parentBizId, boolean isAsync) {
        var ins = new LogicRunnerServiceImpl(logService, insService, configStoreService, transactionalUtils, appConfig, bizLock);
        ins.setEnv(env, true);
        ins.isAsync = isAsync;
        ins.parentLogicId = parentLogicId;
        ins.parentBizId = parentBizId;
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
        String traceId = String.valueOf(idWorker.nextId());
        return runByMap(logicId, parsMap, traceId, traceId, null);
    }

    @Override
    public LogicRunResult runByMap(String logicId, Map<String, Object> parsMap, String traceId, String objectId, JSONObject globalVars) {
        JSONObject config = RuntimeUtil.readLogicConfig(logicId);
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson(), globalVars);
        runner.getFnCtx().setTraceId(traceId == null ? UUID.randomUUID().toString() : traceId);
        var res = runner.run(parsMap);
        res.getLogicLog().setId(objectId);
        logService.addLogicLog(res.getLogicLog());
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

    @Override
    public LogicRunResult runBizByMap(String logicId, String bizId, Map<String, Object> parsMap) {
        String traceId = String.valueOf(idWorker.nextId());
//        String traceId = bizId + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return runBizByMap(logicId, bizId, parsMap, traceId, traceId, null);
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
    public LogicRunResult runBizByMap(String logicId, String bizId, Map<String, Object> parsMap, String traceId, String logicLogId, JSONObject globalVars) {
        String lockKey = bizLock.buildKey(logicId, bizId);
        if (bizLock.spinLock(lockKey)) {
            try {
                log.info("[{}]bizId:{}-get lock key:{}", logicId, bizId, lockKey);
                return runBiz(logicId, bizId, parsMap, traceId, logicLogId, globalVars);
            } catch (Exception e) {
                log.error("[{}]bizId:{}-runBizByMap catch逻辑异常:{}", logicId, bizId, e.getMessage());
                throw new RuntimeException(e);
            } finally {
                bizLock.unlock(lockKey);
                log.info("[{}]bizId:{}-unlock key:{}", logicId, bizId, lockKey);
            }
        } else {
            log.info("[{}]bizId:{}-get lock key:{} fail", logicId, bizId, lockKey);
            return new LogicRunResult().setSuccess(false).setMsg("获取锁失败").setLogicLog(new LogicLog());
        }
    }

    /***
     * 执行前先校验交互点是否正确
     * @param logicId
     * @param bizId
     * @param verifyCode
     * @param parsJsonString
     * @return
     */
    @Deprecated
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
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson(), parsMap, new JSONObject(), new JSONObject(), startId, bizId);
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
     * 执行业务实例
     *
     * @param logicId    逻辑编号
     * @param bizId      业务标识
     * @param parsMap    入参
     * @param traceId    链路标识
     * @param logicLogId
     * @return
     */
    LogicRunResult runBiz(String logicId, String bizId, Map<String, Object> parsMap, String traceId, String logicLogId, JSONObject globalVars) {
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
//                startTime = insEntity.getStartTime();
                if (insEntity.getIsOver()) {
                    var msg = String.format("[%s]bizId:%s，业务实例已完成，无法重复执行。", bizId, logicId);
                    throw new LogicBizException(msg);
                }
            }

        }
        JSONObject config = RuntimeUtil.readLogicConfig(logicId, logicVersion);
        if (config == null) {
            var msg = String.format("[%s]bizId:%s，未发现指定的逻辑，执行中止。", logicId, bizId);
            log.error(msg);
            return new LogicRunResult().setSuccess(false).setMsg(msg);
        }
        var runner = new com.aims.logic.runtime.runner.LogicRunner(config, getEnvJson(), parsMap, cacheVarsJson, globalVars, startId, bizId);
        LogicItemTreeNode startItem = runner.getStartNode();
        runner.getFnCtx().setTraceId(traceId == null ? UUID.randomUUID().toString() : traceId);
//        runner.getFnCtx().setIsRetry(isRetry);
        LogicLog logicLog = LogicLog.newBizLogBeforeRun(instanceId, runner.getFnCtx(), startItem, runner.getFnCtx().getTraceId(), logicLogId);
        logicLog.setParentLogicId(this.parentLogicId).setParentBizId(this.parentBizId).setIsAsync(this.isAsync);
        if (instanceId == null) {//先生成实例记录
            logService.addInstance(logicLog);
        }
        var tranScope = startItem.getTranScope();
        if (tranScope == null || tranScope.equals(LogicItemTransactionScope.def)) {
            if (LogicItemType.start.equalsTo(startItem.getType()) || LogicItemType.waitForContinue.equalsTo(startItem.getType())) {
                tranScope = RuntimeUtil.getEnvObject().getDefaultTranScope();
            } else {
                tranScope = LogicItemTransactionScope.everyNode;
            }
        }
        runner.getFnCtx().setTranScope(tranScope);


        log.info("[{}]bizId:{},tranScope:{}", logicId, bizId, tranScope);
        logService.startBizRunning(logicLog);
        LogicRunResult res = null;
        switch (tranScope) {
            case everyRequest:
                log.info("[{}]bizId:{},runBizInstanceWithEveryRequestTran", logicId, bizId);
                res = runBizInstanceWithEveryRequestTran(runner, startItem, logicLog);
                break;
            case off:
                log.info("[{}]bizId:{},runBizInstanceWithoutTran", logicId, bizId);
                res = runBizInstanceWithoutTran(runner, startItem, logicLog);
                break;
            case everyNode2:
                log.info("[{}]bizId:{},runBizInstanceWithEveryNodeTran2", logicId, bizId);
                res = runBizInstanceWithEveryNodeTran2(runner, startItem, logicLog);
                break;
            case everyJavaNode:
            case everyNode:
            default:
                log.info("[{}]bizId:{},runBizInstanceWithEveryNodeTran", logicId, bizId);
                res = runBizInstanceWithEveryNodeTran(runner, startItem, logicLog);
        }
        logService.stopBizRunning(logicLog);
        logService.addLogicLog(logicLog);
        return res;
    }

    private TransactionStatus beginNewTranIfNewGroup(TransactionStatus lastTran, FunctionContext ctx, LogicItemTreeNode curItem) {
        boolean needNewTran = ctx.getCurTranGroupId() == null ||
                ctx.getLastTranGroupId() == null ||
                !ctx.getCurTranGroupId().equals(ctx.getLastTranGroupId());

        String logMsg = needNewTran ? "begin开启事务组" : "begin复用事务组";
        String tranGroupId = needNewTran ? ctx.getCurTranGroupId() : ctx.getLastTranGroupId();

        log.info("[{}]bizId:{}-当前节点：{}-{},{}: {}",
                ctx.getLogicId(), ctx.getBizId(),
                curItem.getType(), curItem.getName(),
                logMsg, tranGroupId);

        if (!needNewTran) {
            return lastTran;
        }

        if (lastTran != null) {
            if (!lastTran.isCompleted()) {
                transactionalUtils.commit(lastTran);
            }
        }

        return transactionalUtils.newTran();
    }

    private void commitCurTranIfNextIsNewGroup(TransactionStatus curTran, FunctionContext ctx, LogicItemTreeNode curItem) {
        boolean needCommit = ctx.getCurTranGroupId() == null ||
                ctx.getNextTranGroupId() == null ||
                !ctx.getCurTranGroupId().equals(ctx.getNextTranGroupId());

        if (!needCommit) {
            log.info("[{}]bizId:{}-当前节点：{}-{},commit 未提交，保留当前事务组：{}",
                    ctx.getLogicId(), ctx.getBizId(),
                    curItem.getType(), curItem.getName(),
                    ctx.getLastTranGroupId());
            return;
        }

        log.info("[{}]bizId:{}-当前节点：{}-{},commit 提交事务组：{}",
                ctx.getLogicId(), ctx.getBizId(),
                curItem.getType(), curItem.getName(),
                ctx.getCurTranGroupId());

        if (!curTran.isCompleted()) {
            transactionalUtils.commit(curTran);
        } else {
            log.info("[{}]bizId:{},commit 未执行，isCompleted=true",
                    ctx.getLogicId(), ctx.getBizId());
        }
    }

    /***
     * 事务范围为每个节点
     * @param runner
     * @param nextItem
     * @return
     */

    private LogicRunResult runBizInstanceWithEveryNodeTran(LogicRunner runner, LogicItemTreeNode nextItem, LogicLog logicLog) {
        var logicId = logicLog.getLogicId();
        var bizId = logicLog.getBizId();
        log.info("[{}]bizId:{}-runItemWithEveryJavaNodeTran", logicId, bizId);
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
                runner.updateStatus(itemRes, nextItem);
                logicLog.setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.getRunnerStatus() == RunnerStatusEnum.End)
                        .setNextItem(nextItem)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                if (itemRes.isSuccess()) {
                    logService.updateInstance(logicLog);
                    log.info("[{}]bizId:{},begin commit in runItemWithEveryJavaNodeTran-itemResIsSuccess=true", logicId, bizId);
                    commitCurTranIfNextIsNewGroup(curTranStatus, runner.getFnCtx(), curItem);
                    if (Objects.equals(curItem.getType(), LogicItemType.wait.getValue()) && bizLock.isStopping(bizLock.buildKey(logicId, bizId))) {
                        throw new BizManuallyStoppedException(logicId, bizId);
                    }
                } else {
                    log.info("[{}]bizId:{},节点执行失败，begin rollback，success=false,msg:{}, in runItemWithEveryJavaNodeTran", logicId, bizId, itemRes.getMsg());
                    transactionalUtils.rollback(curTranStatus);
                    log.info("[{}]bizId:{},节点执行失败，rollback ok，事务组:{}", logicId, bizId, ctx.getCurTranGroupId());
                    logicLog.setOver(false);
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
                return LogicRunResult.fromLogicLog(logicLog);
            }
        }
        return LogicRunResult.fromLogicLog(logicLog);
    }

    /**
     * 业务异常不中断
     *
     * @param runner
     * @param nextItem
     * @param logicLog 编排链路日志
     * @return
     */
    private LogicRunResult runBizInstanceWithEveryNodeTran2(LogicRunner runner, LogicItemTreeNode nextItem, LogicLog logicLog) {
        var logicId = logicLog.getLogicId();
        var bizId = logicLog.getBizId();
        log.info("[{}]bizId:{}-runItemWithEveryJavaNodeTran2", logicId, bizId);
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
                runner.updateStatus(itemRes, nextItem);
                logicLog.setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.getRunnerStatus() == RunnerStatusEnum.End)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                if (itemRes.isSuccess()) {
                    //执行成功正常指定下一个节点
                    logicLog.setNextItem(nextItem);
                    logService.updateInstance(logicLog);
                    log.info("[{}]bizId:{},begin commit in runItemWithEveryJavaNodeTran-itemResIsSuccess=true", logicId, bizId);
                    commitCurTranIfNextIsNewGroup(curTranStatus, runner.getFnCtx(), curItem);
                    if (Objects.equals(itemRes.getItemInstance().getType(), LogicItemType.wait.getValue()) && bizLock.isStopping(logicId + "-" + bizId)) {
                        throw new BizManuallyStoppedException(logicId, bizId);
                    }
                } else {
                    //执行失败，下一次继续执行当前节点
                    logicLog.setNextItem(curItem).setOver(false);
                    log.info("[{}]bizId:{},节点执行失败，begin rollback，success=false,msg:{}, in runItemWithEveryJavaNodeTran", logicId, bizId, itemRes.getMsg());
                    transactionalUtils.rollback(curTranStatus);
                    log.info("[{}]bizId:{},节点执行失败，rollback ok，事务组:{}", logicId, bizId, ctx.getCurTranGroupId());
                    logService.updateInstance(logicLog);
                }
                //代码报错时会中断执行
                if (itemRes.isNeedInterrupt()) {
                    break;
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
                logicLog.setOver(false).setSuccess(false).setMsg(msg);
                return LogicRunResult.fromLogicLog(logicLog);
            }
        }
        return LogicRunResult.fromLogicLog(logicLog);
    }

    /**
     * 事务范围为每个交互点
     *
     * @param runner
     * @param nextItem
     * @return
     */
    private LogicRunResult runBizInstanceWithEveryRequestTran(LogicRunner runner, LogicItemTreeNode nextItem, LogicLog logicLog) {
        var logicId = logicLog.getLogicId();
        var bizId = logicLog.getBizId();
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
                        runner.updateStatus(itemRes, nextItem);
                        if (Objects.equals(itemRes.getItemInstance().getType(), LogicItemType.wait.getValue()) && bizLock.isStopping(logicId + "-" + bizId)) {
                            throw new BizManuallyStoppedException(logicId, bizId);
                        }
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
            logicLog.setSuccess(itemRes.isSuccess());
            //本次交互完成，没有错误则提交，否则本次交互全部回滚，只更新实例success状态和消息
            if (itemRes.isSuccess()) {
                logicLog.setVarsJson_end(runner.getFnCtx().get_var())
                        .setOver(runner.updateStatus(itemRes, nextItem) == RunnerStatusEnum.End)
                        .setNextItem(nextItem)
                        .setSuccess(itemRes.isSuccess()).setMsg(itemRes.getMsg());
                logService.updateInstance(logicLog);
                transactionalUtils.commit(begin);
            } else {
                if (!begin.isCompleted()) {
                    log.info("[{}]bizId:{},节点执行失败，begin rollback", logicId, bizId);
                    transactionalUtils.rollback(begin);
                } else {
                    log.info("[{}]bizId:{},节点执行失败，事务isCompleted=true，rollback 未执行", logicId, bizId);
                }
                logicLog.setOver(false);
                return new LogicRunResult().setLogicLog(logicLog)
                        .setSuccess(false)
                        .setMsg(itemRes.getMsg());
            }
        }
        return new LogicRunResult()
                .setLogicLog(logicLog)
                .setData(itemRes == null ? null : itemRes.getData())
                .setSuccess(itemRes == null || itemRes.isSuccess())
                .setMsg(itemRes.getMsg());
    }

    /**
     * 无事务
     *
     * @param runner
     * @param nextItem
     * @return
     */
    private LogicRunResult runBizInstanceWithoutTran(LogicRunner runner, LogicItemTreeNode nextItem, LogicLog logicLog) {
        LogicItemRunResult itemRes = null;
        try {
            while (runner.getRunnerStatus() == RunnerStatusEnum.Continue) {
                logicLog.setNextItem(nextItem);
                itemRes = runner.runItem(nextItem);
                nextItem = runner.findNextItem(nextItem);
                logicLog.addItemLog(itemRes);
                runner.updateStatus(itemRes, nextItem);
                if (itemRes.isSuccess()) {
                    logicLog.setVarsJson_end(runner.getFnCtx().get_var());
                } else {
                    logicLog.setOver(false)
                            .setSuccess(false).setMsg(itemRes.getMsg());
                }
            }
        } catch (Exception e) {
            logicLog.setOver(false)
                    .setSuccess(false).setMsg(e.getMessage());
        }
        if (logicLog.isSuccess())
            logicLog.setOver(runner.updateStatus(itemRes, nextItem) == RunnerStatusEnum.End)
                    .setNextItem(nextItem).setMsg(itemRes.getMsg());
        logService.updateInstance(logicLog);
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
    @Override
    public LogicRunResult retryErrorBiz(String logicId, String bizId) {
        JSONObject parsJson = null;
        LogicInstanceEntity insEntity;
        if (bizId != null && !bizId.isBlank()) {
            insEntity = insService.getInstance(logicId, bizId);
            if (insEntity != null) {
                if (!(!insEntity.getSuccess() || insEntity.getIsRunning())) {
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
            String lockKey = bizLock.buildKey(logicId, bizId);
            try {
                bizLock.spinLock(lockKey);
                log.info("[{}]bizId:{}-get lock key:{}", logicId, bizId, lockKey);
                return runBiz(logicId, bizId, parsJson, UUID.randomUUID().toString(), null, null);
            } catch (Exception e) {
                log.error("[{}]bizId:{}-runBizByMap catch逻辑异常:{}", logicId, bizId, e.getMessage());
                throw new RuntimeException(e);
            } finally {
                bizLock.unlock(lockKey);
                Map<String, Object> vals = new HashMap<>();
                vals.put("retryTimes", insEntity.getRetryTimes() + 1);
                insService.updateById(insEntity.getId(), vals);
                log.info("[{}]bizId:{}-unlock key:{}", logicId, bizId, lockKey);
            }
        } else {
            return new LogicRunResult().setSuccess(false).setMsg(String.format("业务逻辑[%s]的实例[%s]不存在，不可重试！", logicId, bizId));
        }
    }

    @Override
    public List<LogicRunResult> retryLongtimeRunningBiz(int timeout) {
        List<LogicRunResult> res = new ArrayList<>();
        var list = queryLongtimeRunningBiz(timeout);
        if (list != null)
            list.forEach(insEntity -> {
                res.add(retryErrorBiz(insEntity.getLogicId(), insEntity.getBizId()));
            });
        return res;
    }

    @Override
    public List<LongtimeRunningBizDto> queryLongtimeRunningBiz(int timeout) {
        var list = insService.queryLongtimeRunningBiz(timeout);
        if (list == null)
            return null;
        return list.stream().map(insEntity -> new LongtimeRunningBizDto()
                .setLogicId(insEntity.getLogicId())
                .setBizId(insEntity.getBizId())
                .setStartTime(insEntity.getStartTime())
                .setIsAsync(insEntity.getIsAsync())
                .setParentBizId(insEntity.getParentBizId())
                .setParentLogicId(insEntity.getParentLogicId())).collect(Collectors.toList());
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning) {
        return queryUncompletedBiz(createTimeFrom, createTimeTo, isRunning, null);
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess) {
        return queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, null);
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBizExclude(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess, List<String> excludeLogicIds) {
        var list = insService.queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, excludeLogicIds);
        if (list == null)
            return null;
        return list.stream().map(insEntity -> new UnCompletedBizDto()
                .setLogicId(insEntity.getLogicId())
                .setBizId(insEntity.getBizId())
                .setCreateTime(insEntity.getCreateTime())
                .setIsRunning(insEntity.getIsRunning())
                .setIsSuccess(insEntity.getSuccess())
                .setIsAsync(insEntity.getIsAsync())
                .setParentLogicId(insEntity.getParentLogicId())
                .setParentBizId(insEntity.getParentBizId())).collect(Collectors.toList());
    }

    @Override
    public boolean resetBizInstanceNextId(String logicId, String bizId, String nextId, String nextName, String varsJsonEnd) {
        return insService.updateInstanceNextId(logicId, bizId, nextId, nextName, varsJsonEnd) > 0;
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

    @Override
    public int deleteBizInstance(String logicId, String bizId) {
        LogicInstanceEntity insEntity = insService.getInstance(logicId, bizId);
        if (insEntity != null) {
            return insService.removeById(insEntity.getId());
        }
        log.info("要删除的业务实例logicId:{},bizId:{}不存在！", logicId, bizId);
        return 0;
    }

    @Override
    public int deleteCompletedBizInstanceByLogicId(String logicId) {
        return insService.deleteCompletedBizInstanceByLogicId(logicId);
    }

    @Override
    public void stopBiz(String logicId, String bizId) {
        bizLock.setBizStopping(bizLock.buildKey(logicId, bizId));
    }
}
