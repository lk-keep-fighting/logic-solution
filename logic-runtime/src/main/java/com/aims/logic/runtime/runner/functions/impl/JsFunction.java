package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.functions.JSFunctionService;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author liukun
 */
@Service
public class JsFunction implements JSFunctionService {
    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object script) {
        if (script == null) {
            return new LogicItemRunResult();
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        engine.put("_var", ctx.get_var());
        engine.put("_env", ctx.get_env());
        engine.put("_par", ctx.get_par());
        engine.put("_ret", ctx.get_ret());
        engine.put("_lastRet", ctx.get_lastRet());
        try {
            String processedCode = script.toString().replaceAll("^//.*", "");
            // Remove multi-line comments
//            Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
//            Matcher matcher = pattern.matcher(processedCode);
//            processedCode = matcher.replaceAll("");
            engine.eval(String.format("function fn(){ %s }", processedCode));
            Invocable inv = (Invocable) engine;
            Object res = inv.invokeFunction("fn");
            return new LogicItemRunResult().setData(res);
        } catch (Exception exception) {
            ctx.setHasErr(true);
            ctx.setErrMsg(exception.toString());
            System.err.println(exception.toString());
            return new LogicItemRunResult().setData(exception.toString());
        }
    }

    @Override
    public String getItemType() {
        return "js";
    }
}
