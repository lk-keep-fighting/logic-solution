package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

/**
 * @author liukun
 */
@Slf4j
@Service
public class JsFunction implements ILogicItemFunctionRunner {

    private final Engine sharedEngine;

    public JsFunction(Engine sharedEngine) {
        this.sharedEngine = sharedEngine;
    }

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object script) {
        LogicItemRunResult itemRes = new LogicItemRunResult();
        if (script == null) {
            return itemRes;
        }

        // 直接使用Polyglot Context而不是ScriptEngine
        try (Context context = Context.newBuilder("js")
                .engine(sharedEngine)
                .allowHostAccess(HostAccess.ALL)
                .build()) {

            // 设置变量到JavaScript上下文中
            var bindings = context.getBindings("js");
            bindings.putMember("_var", ctx.get_var());
            bindings.putMember("_env", ctx.get_env());
            bindings.putMember("_bizId", ctx.getBizId());
            bindings.putMember("_global", ctx.get_global());
            bindings.putMember("_par", JSON.toJSON(ctx.get_par()));
            bindings.putMember("_last", JSON.toJSON(ctx.get_last()));
            bindings.putMember("_lastRet", JSON.toJSON(ctx.get_lastRet()));

            String processedCode = script.toString().replaceAll("^//.*", "");

            // 执行初始化代码
            context.eval("js", "_last.data=_lastRet");

            // 定义并执行函数
            String functionCode = String.format("function fn(){ %s };fn();", processedCode);
            Value result = context.eval("js", functionCode);

            // 处理返回结果
            Object funcRes = JSON.toJSON(result.as(Object.class));
            return new LogicItemRunResult().setData(funcRes);

        } catch (Exception e) {
            log.error("[{}]bizId:{},js function error: {}", ctx.getLogicId(), ctx.getBizId(), e.getMessage());
            return new LogicItemRunResult()
                    .setMsg(e.getMessage())
                    .setSuccess(false);
        }
    }

    // 添加一个辅助方法来转换GraalVM的值到Java对象
    private Object convertValue(Value value) {
        if (value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            return value.asDouble();
        } else if (value.isString()) {
            return value.asString();
        } else if (value.isHostObject()) {
            return value.asHostObject();
        } else if (value.hasHashEntries()) {
            // 处理对象
            com.alibaba.fastjson2.JSONObject obj = new com.alibaba.fastjson2.JSONObject();
            for (String key : value.getMemberKeys()) {
                obj.put(key, convertValue(value.getMember(key)));
            }
            return obj;
        } else if (value.hasArrayElements()) {
            // 处理数组
            com.alibaba.fastjson2.JSONArray array = new com.alibaba.fastjson2.JSONArray();
            for (int i = 0; i < value.getArraySize(); i++) {
                array.add(convertValue(value.getArrayElement(i)));
            }
            return array;
        } else {
            // 默认转为字符串
            return value.toString();
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

//    @PreDestroy
//    public void destroy() {
//        try {
//            sharedEngine.close();
//        } catch (Exception var2) {
//            log.error("js engine close error: {}", var2.getMessage());
//        }
//    }
}
