package com.aims.logic.runtime.contract.logger;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class LogicItemLog {
    public LogicItemLog() {
        success = true;
    }

    private String name;
    /*
    节点配置
     */
    private LogicItemTreeNode config;
    /**
     * 节点转换后的实例
     */
    private LogicItemTreeNode configInstance;
    /**
     * 当前item的入参json
     */
//    private JSONObject paramsJson;
    /**
     * 当前item返回值
     */
    private String returnData;

    public LogicItemLog setReturnData(Object returnData) {
        if (returnData != null) this.returnData = JSON.toJSONString(returnData);
        else this.returnData = null;
        return this;
    }
    // 存储返回值副本，防止returnData存在嵌套对象时被后面的日志覆盖
//    private String returnDataStr;
    private boolean success;
    private String msg;
}
