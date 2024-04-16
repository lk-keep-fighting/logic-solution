package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
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
    //    private JSONObject _ret = new JSONObject();
    private Object _lastRet;
    private LogicTreeNode logic;
    //    private LogicRunner _logicRunner;
    private boolean hasErr = false;
    private String errMsg = null;
    private String logicId = null;
    private String bizId = null;
    private LogicItemTreeNode nextItem;

    public FunctionContext() {

    }

    public boolean isLogOff() {
        return "off".equals(logic.getLog()) || ("off".equals(_env.get("LOG")) && !"on".equals(logic.getLog()));
    }
}
