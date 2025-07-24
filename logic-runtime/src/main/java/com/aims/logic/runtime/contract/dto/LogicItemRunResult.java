package com.aims.logic.runtime.contract.dto;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.logger.LogicItemLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class LogicItemRunResult {
    public LogicItemRunResult() {
    }

    public boolean success = true;
    boolean needInterrupt = false;

    /**
     * 消息
     */
    public String msg;
    /**
     * 返回数据
     */
    public Object data;

//    /**
//     * 获取data数据的字符串表示，
//     * 如果是json则转换为json字符串，
//     * 通常用于获取值进行存储或判断
//     */
//    public String getDataString() {
//        try {
//            return JSONObject.toJSONString(data);
//        } catch (Exception ex) {
//            return (String) data;
//        }
//    }

    /**
     * 逻辑项实例，配置解析后的数据实例
     */
    LogicItemTreeNode itemInstance;

    LogicItemLog itemLog;

}
