package com.aims.logic.runtime.contract.logger;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.util.JsonUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class LogicLog extends Log {
    public LogicLog() {

    }

    boolean isLogOff = false;

    public static LogicLog newBizLogBeforeRun(String instanceId, FunctionContext ctx, LogicItemTreeNode nextItem) {
        return new LogicLog().setInstanceId(instanceId).setBizId(ctx.getBizId()).setLogicId(ctx.getLogicId()).setVersion(ctx.getLogic().getVersion())
                .setParamsJson(JSONObject.from(ctx.get_par()))
                .setVarsJson(JsonUtil.clone(ctx.get_var()))
                .setEnvsJson(ctx.get_env())
                .setNextItem(nextItem)
                .setLogOff(ctx.isLogOff());
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
    /**
     * 业务实例流水号
     */
    String instanceId;
    String logicId;
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

    //    String returnDataStr;
    public String getReturnDataStr() {
        if (returnData != null) JSONObject.toJSONString(returnData, JSONWriter.Feature.WriteNulls);
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

    List<LogicItemLog> itemLogs = new ArrayList<>();

    public void addItemLog(LogicItemRunResult itemRunResult) {
        returnData = itemRunResult.getData();
        if (!isLogOff)
            itemLogs.add(itemRunResult.getItemLog());
    }
}
