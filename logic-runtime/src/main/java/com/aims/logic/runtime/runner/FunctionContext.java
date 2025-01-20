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
    private String bizId = null;
    private LogicItemTreeNode nextItem;
    private String curTranGroupId;
    private String nextTranGroupId;
    private String lastTranGroupId;


    public FunctionContext() {

    }

    public void setTraceId(String traceId) {
        _env.put("TRACE_ID", traceId);
        this.traceId = traceId;
    }

    public String getTraceId() {
        if (this.traceId == null) {//尝试从环境变量获取，如果是复用逻辑，环境变量中会有值
            if (_env.containsKey("TRACE_ID") && _env.getString("TRACE_ID") != null) {
                setTraceId(_env.getString("TRACE_ID"));
            }
        }
        return this.traceId;
    }

    public boolean isLogOff() {
        if ("off".equals(logic.getLog()))
            return true;
        if ("on".equals(logic.getLog()))
            return false;
        return "off".equals(_env.get("LOG"));
    }
}
