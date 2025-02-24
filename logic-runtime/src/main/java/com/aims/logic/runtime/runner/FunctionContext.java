package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class FunctionContext {
    private Map<String, Object> _par = new HashMap<>();
    private JSONObject _var = new JSONObject();
    private JSONObject _env = new JSONObject();
    private Object _lastRet;
    private LogicItemRunResult _last;
    private LogicTreeNode logic;
    private String traceId = null;
    private String logicId = null;
//    private String logicLogId = null;
    private String bizId = null;
//    /**
//     * 是否为重试执行
//     */
//    private Boolean isRetry = false;
    private LogicItemTreeNode nextItem;
    private String curTranGroupId;
    private String nextTranGroupId;
    private String lastTranGroupId;


    public String getSubLogicRandomBizId() {
        if (_var.get("__subLogicRandomBizId") == null)
            return buildSubLogicRandomBizId();
        return _var.get("__subLogicRandomBizId").toString();
    }

    public String buildSubLogicRandomBizId() {
        var subLogicRandomBizId = logicId + "_" + System.currentTimeMillis();
        _var.put("__subLogicRandomBizId", subLogicRandomBizId);
        return subLogicRandomBizId;
    }

    public FunctionContext() {

    }

    public boolean isLogOff() {
        if ("off".equals(logic.getLog()))
            return true;
        if ("on".equals(logic.getLog()))
            return false;
        return "off".equals(_env.get("LOG"));
    }
}
