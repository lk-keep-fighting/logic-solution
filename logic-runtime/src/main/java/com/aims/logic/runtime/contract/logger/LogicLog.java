package com.aims.logic.runtime.contract.logger;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.util.JsonUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

    public static LogicLog newBizLogBeforeRun(String instanceId, FunctionContext ctx, LogicItemTreeNode nextItem) {
        return new LogicLog().setInstanceId(instanceId).setBizId(ctx.getBizId()).setLogicId(ctx.getLogicId()).setVersion(ctx.getLogic().getVersion())
                .setParamsJson(JSONObject.from(ctx.get_par()))
                .setVarsJson(JsonUtil.clone(ctx.get_var()))
                .setEnvsJson(ctx.get_env())
                .setNextItem(nextItem);
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
        if (itemLogs != null && !itemLogs.isEmpty()) {
            var returnData = itemLogs.get(itemLogs.size() - 1).getReturnData();
            if (returnData == null) return null;
            if (returnData instanceof JSONObject) {
                return JSON.toJSONString(returnData);
            } else
                return returnData.toString();
        } else return null;
    }

    public Object getReturnData() {
        if (itemLogs != null && !itemLogs.isEmpty()) {
            var returnData = itemLogs.get(itemLogs.size() - 1).getReturnData();
            if (returnData == null) return null;
            return returnData;
        } else return null;
    }

    List<LogicItemLog> itemLogs = new ArrayList<>();
}
