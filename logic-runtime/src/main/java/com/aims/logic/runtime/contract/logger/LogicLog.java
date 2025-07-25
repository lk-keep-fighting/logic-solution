package com.aims.logic.runtime.contract.logger;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.enums.LogicStopModel;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.util.JsonUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Queue;

@Accessors(chain = true)
@Getter
@Setter
public class LogicLog {
    public LogicLog() {

    }

    /*
    逻辑日志唯一编号
    当此逻辑为复用逻辑时，id由父逻辑传入进行串联，追踪当前为父逻辑的那一次调用
     */
    String id;

    String msgId;
    boolean success = true;
    String msg;
    @Deprecated
    Error error;

    boolean isLogOff = false;

    public static LogicLog newBizLogBeforeRun(String instanceId, FunctionContext ctx, LogicItemTreeNode nextItem, String traceId, String logicLogId) {
        return new LogicLog().setInstanceId(instanceId).setBizId(ctx.getBizId()).setLogicId(ctx.getLogicId()).setVersion(ctx.getLogic().getVersion())
                .setParamsJson(JSONObject.from(ctx.get_par()))
                .setVarsJson(JsonUtil.clone(ctx.get_var()))
                .setVarsJson_end(JsonUtil.clone(ctx.get_var()))
                .setEnvsJson(ctx.get_env())
                .setNextItem(nextItem)
                .setLogOff(ctx.isLogOff())
                .setMsgId(traceId)
                .setId(logicLogId);
    }

    /**
     * 入参
     */
    JSONObject paramsJson;
    /**
     * 执行开始时具备变量的值
     */
    JSONObject varsJson;
    /**
     * 环境变量
     */
    JSONObject envsJson;
    /**
     * 执行结束后当前的具备变量值
     */
    JSONObject varsJson_end;

    public JSONObject getGlobalVars() {
        if (varsJson_end == null) return null;
        return varsJson_end.getJSONObject("__global");
    }

    /**
     * 业务实例流水号
     */
    String instanceId;
    String logicId;
    /**
     * 父逻辑编号
     */
    String parentLogicId;
    /**
     * 是否异步执行
     */
    Boolean isAsync;
    /**
     * 父业务标识
     */
    String parentBizId;
    String version;
    /**
     * 业务标识
     */
    String bizId;
    /**
     * 下一个执行节点
     */
    LogicItemTreeNode nextItem;
    /**
     * 整个逻辑是否已结束，没有后续交互节点
     */
    boolean isOver = false;
    /**
     * 编排停止模式
     */
    LogicStopModel stopModel = LogicStopModel.NORMAL;

    boolean isRunning = true;

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
        if (!isRunning) {
            this.stopTime = LocalDateTime.now();
        }
    }

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime stopTime;

    public long getDuration() {
        if (stopTime == null) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(startTime, stopTime);
    }

    /**
     * 获取当前逻辑运行时长，直到现在
     *
     * @return
     */
    public long getDurationUntilNow() {
        return ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
    }

    //    String returnDataStr;
    public String getReturnDataStr() {
        if (returnData != null) return JSONObject.toJSONString(returnData, JSONWriter.Feature.WriteNulls);
        return null;
    }
//
//    public Object getReturnData() {
//        if (returnData == null) {
//            if (itemLogs != null && !itemLogs.isEmpty()) {
//                return itemLogs.get(itemLogs.size() - 1).getReturnData();
//            }
//        }
//        return returnData;
//    }

    Object returnData;

    public final Queue<LogicItemLog> itemLogs = new ArrayDeque<>(30);

    public void addItemLog(LogicItemRunResult itemRunResult) {
        returnData = itemRunResult.getData();
        if (!isLogOff) {
            if (itemLogs.size() >= 30) {
                itemLogs.poll();
            }
            itemLogs.add(itemRunResult.getItemLog());
        }
    }
}
