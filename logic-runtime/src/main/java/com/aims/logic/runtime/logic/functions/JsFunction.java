package com.aims.logic.runtime.logic.functions;

import com.aims.logic.runtime.logic.FunctionContext;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JsFunction implements IFunction<Object> {
    @Override
    public Object invoke(FunctionContext ctx, Object script) {
        if (script == null) return null;
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
            return inv.invokeFunction("fn");
        } catch (Exception exception) {
            ctx.setHasErr(true);
            ctx.setErrMsg(exception.toString());
            System.err.println(exception.toString());
            return null;
        }
    }
}
