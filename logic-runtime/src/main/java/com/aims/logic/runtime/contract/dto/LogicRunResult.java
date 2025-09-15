package com.aims.logic.runtime.contract.dto;

import com.aims.logic.runtime.contract.enums.LogicStopModel;
import com.aims.logic.runtime.contract.logger.LogicLog;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class LogicRunResult {
    public LogicRunResult() {
    }

    boolean success = true;
    /**
     * 消息
     */
    String msg;
    /**
     * 返回数据
     */
    Object data;

    LogicStopModel stopModel = LogicStopModel.NORMAL;

    /**
     * 获取data数据的字符串表示，
     * 如果是json则转换为json字符串，
     * 通常用于获取值进行存储或判断
     */
    public String getDataString() {
        if (data == null) {
            return null;
        }
        if (data instanceof String) {
            return (String) data;
        }
        try {
            return JSONObject.toJSONString(data, JSONWriter.Feature.WriteNulls);
        } catch (Exception ex) {
            // 如果序列化失败，返回对象的toString表示
            return String.valueOf(data);
        }
    }

    public static LogicRunResult fromItemResult(LogicItemRunResult itemRunResult) {
        return new LogicRunResult()
                .setData(itemRunResult.getData())
                .setMsg(itemRunResult.getMsg())
                .setSuccess(itemRunResult.isSuccess());
    }

    public static LogicRunResult fromLogicLog(LogicLog logicLog) {
        return new LogicRunResult()
                .setLogicLog(logicLog)
                .setData(logicLog.getReturnData())
                .setMsg(logicLog.getMsg())
                .setSuccess(logicLog.isSuccess());
    }

    /**
     * 执行日志
     */
    LogicLog logicLog;

    public LogicRunResult setLogicLog(LogicLog logicLog) {
        this.logicLog = logicLog;
        this.stopModel = logicLog.getStopModel();
        return this;
    }
}
