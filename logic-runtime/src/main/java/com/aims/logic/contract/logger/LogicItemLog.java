package com.aims.logic.contract.logger;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.alibaba.fastjson2.JSONObject;
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
    private JSONObject paramsJson;
    /**
     * 当前item返回值
     */
    private Object returnData;
    private boolean success;
}
