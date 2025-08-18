package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import com.aims.logic.runtime.util.IdWorker;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class FunctionContext {
    private Map<String, Object> _par = new HashMap<>();
    private JSONObject _var = new JSONObject();
    private JSONObject _env = new JSONObject();
    private JSONObject _global;
    // private Object _lastRet;
    private LogicItemRunResult _last;
    private LogicTreeNode logic;
    private String traceId = null;
    private String logicId = null;
    private String bizId = null;
    // 当前事务组开始节点
    private LogicItemTreeNode curTranGroupBeginItem;
    // 当前事务组开始时变量
    private JSONObject curTranGroupBeginVar;
    private LogicItemTreeNode nextItem;
    private String curTranGroupId;
    private String nextTranGroupId;
    private String lastTranGroupId;

    public LogicItemRunResult get_last() {
        if (_last == null) {
            _last = new LogicItemRunResult();
        }
        return _last;
    }

    public Object get_lastRet() {
        return _last == null ? null : _last.getData();
    }

    public void setTranScope(LogicItemTransactionScope tranScope) {
        _var.put("__tranScope", tranScope);
    }

    /**
     * 本次交互的事务作用域配置
     * 从当前交互点读取
     */
    public LogicItemTransactionScope getTranScope() {
        if (_var.get("__tranScope") == null)
            return LogicItemTransactionScope.def;
        return LogicItemTransactionScope.valueOf(_var.get("__tranScope").toString());
    }

    public JSONObject get_global() {
        if (_var.getJSONObject("__global") == null) {
            _var.put("__global", new JSONObject());
        }
        return _var.getJSONObject("__global");
    }

    public void set_global(JSONObject global) {
        if (global == null)
            global = new JSONObject();
        if (_var.getJSONObject("__global") == null)
            _var.put("__global", new JSONObject());
        _var.getJSONObject("__global").putAll(global);
    }

    // public synchronized String getSubLogicRandomBizId() {
    // if (_var.get("__subLogicRandomBizId") == null)
    // buildSubLogicRandomBizId();
    // var subLogicRandomBizId = _var.get("__subLogicRandomBizId").toString();
    // buildSubLogicRandomBizId();
    // return subLogicRandomBizId;
    // }

    public String buildSubLogicRandomBizId() {
        // _var.put("__subLogicRandomBizId", subLogicRandomBizId);
        return UUID.randomUUID().toString();//String.valueOf(idWorker.nextId());
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
