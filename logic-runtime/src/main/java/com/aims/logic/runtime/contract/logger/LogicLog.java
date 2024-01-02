package com.aims.logic.runtime.contract.logger;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
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
    String returnDataStr;
    List<LogicItemLog> itemLogs = new ArrayList<>();
}
