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
import javax.script.ScriptEngine;

/**
 * @author liukun
 */
@Slf4j
@Service
public class JsFunction implements ILogicItemFunctionRunner {

    // 单例复用 Engine
    private static final Engine SHARED_GRAAL_ENGINE = Engine.newBuilder()
            .option("engine.WarnInterpreterOnly", "false")
            .build();

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object script) {
        LogicItemRunResult itemRes = new LogicItemRunResult();
        if (script == null) {
            return itemRes;
        }
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("graal.js");
//        engine.put("polyglot.js.allowAllAccess", true);

//        Engine graalEngine = Engine.newBuilder()
//                .option("engine.WarnInterpreterOnly", "false")
//                .build();
        ScriptEngine engine = GraalJSScriptEngine.create(SHARED_GRAAL_ENGINE,
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL));
        engine.put("_var", ctx.get_var());
        engine.put("_env", ctx.get_env());
        engine.put("_bizId", ctx.getBizId());
        engine.put("_global", ctx.get_global());
        engine.put("_par", JSONObject.from(ctx.get_par()));
        engine.put("_lastRet", ctx.get_lastRet());
        engine.put("_last", JSONObject.from(ctx.get_last()));
        try {
            String processedCode = script.toString().replaceAll("^//.*", "");
            engine.eval(String.format("function fn(){ %s }", processedCode));
            Invocable inv = (Invocable) engine;
            Object funcRes = inv.invokeFunction("fn");
            return new LogicItemRunResult().setData(funcRes);
        } catch (Exception e) {
            log.error("[{}]bizId:{},js function error: {}", ctx.getLogicId(), ctx.getBizId(), e.getMessage());
            return new LogicItemRunResult()
                    .setMsg(e.getMessage())
                    .setSuccess(false);
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
        try {
            SHARED_GRAAL_ENGINE.close();
        } catch (Exception e) {
            log.error("js engine close error: {}", e.getMessage());
        }
    }
}
