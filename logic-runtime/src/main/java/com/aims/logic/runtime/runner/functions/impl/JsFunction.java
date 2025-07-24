package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.alibaba.fastjson2.JSONObject;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * @author liukun
 */
@Slf4j
@Service
public class JsFunction implements ILogicItemFunctionRunner {

    private static final Engine sharedEngine = Engine.newBuilder()
            .option("engine.WarnInterpreterOnly", "false")
            .build();
    // 使用ThreadLocal维护线程独立的ScriptEngine
    private static final ThreadLocal<ScriptEngine> engineHolder = ThreadLocal.withInitial(() -> GraalJSScriptEngine.create(sharedEngine,
            Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)));

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object script) {
        LogicItemRunResult itemRes = new LogicItemRunResult();
        if (script == null) {
            return itemRes;
        }
        ScriptEngine engine = engineHolder.get();
        engine.put("_var", ctx.get_var());
        engine.put("_env", ctx.get_env());
        engine.put("_bizId", ctx.getBizId());
        engine.put("_global", ctx.get_global());
        engine.put("_par", JSONObject.from(ctx.get_par()));
        engine.put("_lastRet", JSONObject.from(ctx.get_lastRet()));
        engine.put("_last", JSONObject.from(ctx.get_last()));
        try {
            String processedCode = script.toString().replaceAll("^//.*", "");
            engine.eval("_last.data=_lastRet");
            engine.eval(String.format("function fn(){ %s }", processedCode));
            Invocable inv = (Invocable) engine;
            Object funcRes = inv.invokeFunction("fn");
            return new LogicItemRunResult().setData(funcRes);
        } catch (Exception e) {
            log.error("[{}]bizId:{},js function error: {}", ctx.getLogicId(), ctx.getBizId(), e.getMessage());
            return new LogicItemRunResult()
                    .setMsg(e.getMessage())
                    .setSuccess(false);
        } finally {
            engine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
            try {
                engine.eval("(function() { for (let k in this) delete this[k] })()");
            } catch (Exception e) {
                log.error("clear engine error: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getItemType() {
        return "js";
    }

    @Override
    public int getPriority(String env) {
        return 0;
    }

    @PreDestroy
    public void destroy() {
        engineHolder.remove();  // 清理当前线程实例
    }
}
