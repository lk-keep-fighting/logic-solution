package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.runtime.util.JsonUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author liukun
 */
@Slf4j
@Service
public class JsFunction implements ILogicItemFunctionRunner {
    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object script) {
        LogicItemRunResult itemRes = new LogicItemRunResult();
        if (script == null) {
            return itemRes;
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        engine.put("_var", ctx.get_var());
        engine.put("_env", ctx.get_env());
        engine.put("_par", JSONObject.from(ctx.get_par()));
//        engine.put("_ret", ctx.get_ret());
        engine.put("_lastRet", ctx.get_lastRet());
        try {
            String processedCode = script.toString().replaceAll("^//.*", "");
            engine.eval(String.format("function fn(){ %s }", processedCode));
            Invocable inv = (Invocable) engine;
            Object funcRes = inv.invokeFunction("fn");
            Object data = convertResult(funcRes);
            return new LogicItemRunResult().setData(data);
        } catch (IllegalArgumentException | ScriptException | NoSuchMethodException exception) {
            log.error("[{}]bizId:{},js function error: {}", ctx.getLogicId(), ctx.getBizId(), exception.getMessage());
            return new LogicItemRunResult()
                    .setMsg(exception.getMessage())
                    .setSuccess(false);
        }
    }

    private Object convertResult(Object funcRes) {
        if (funcRes instanceof ScriptObjectMirror) {
            try {
                // 对转换过程进行异常捕获，确保数据转换的健壮性
                return JsonUtil.toObject((ScriptObjectMirror) funcRes);
            } catch (Exception e) {
                // 可以根据实际情况记录日志或者采取其他处理措施
                log.error("js意外的异常：转换js执行结果失败", e);
                return null; // 或者返回一个特定的错误标示对象
            }
        }
        return funcRes;
    }

    @Override
    public String getItemType() {
        return "js";
    }

    @Override
    public int getPriority(String env) {
        return 0;
    }
}
