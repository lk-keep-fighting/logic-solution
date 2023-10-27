package com.aims.logic.runtime.contract.dto;

import com.aims.logic.runtime.contract.logger.LogicLog;
import com.alibaba.fastjson2.JSONObject;
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

    /**
     * 获取data数据的字符串表示，
     * 如果是json则转换为json字符串，
     * 通常用于获取值进行存储或判断
     *
     * @return
     */
    public String getDataString() {
        try {
            return JSONObject.toJSONString(data);
        } catch (Exception ex) {
            return (String) data;
        }
    }

    /**
     * 执行日志
     */
    LogicLog logicLog;
}
