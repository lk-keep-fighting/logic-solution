package com.aims.logic.runtime.runner;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FunctionContext {
    private JSONObject _par = new JSONObject();
    private JSONObject _var = new JSONObject();
    private JSONObject _env = new JSONObject();
    private JSONObject _ret = new JSONObject();
    private Object _lastRet;
    private LogicRunner _logicRunner;
    private boolean hasErr = false;
    private String errMsg = null;

    public FunctionContext() {

    }
}
