package com.aims.logic.contract.dto;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.contract.logger.LogicItemLog;
import com.aims.logic.contract.logger.LogicLog;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class LogicItemRunResult {
    public LogicItemRunResult() {
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
     */
    public String getDataString() {
        try {
            return JSONObject.toJSONString(data);
        } catch (Exception ex) {
            return (String) data;
        }
    }

    /**
     * 逻辑项实例，配置解析后的数据实例
     */
    LogicItemTreeNode itemInstance;

    LogicItemLog itemLog;

}
