package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    // 直接使用Polyglot Context而不是ScriptEngine
    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object script) {
        LogicItemRunResult itemRes = new LogicItemRunResult();
        if (script == null) {
            return itemRes;
        }


        try (Context context = Context.newBuilder("js")
                .engine(sharedEngine)
                .allowHostAccess(HostAccess.ALL)
                .build()) {

            // 设置变量到JavaScript上下文中，使用JSON转换确保可访问性
            var bindings = context.getBindings("js");
            bindings.putMember("_var", ctx.get_var());
            bindings.putMember("_env", ctx.get_env());
            bindings.putMember("_bizId", ctx.getBizId());
            bindings.putMember("_global", ctx.get_global());
            bindings.putMember("_par", deepConvertToJson(ctx.get_par()));// 深度转换为纯JSON，确保嵌套属性可访问
            bindings.putMember("_last", deepConvertToJson(ctx.get_last()));
            bindings.putMember("_lastRet", deepConvertToJson(ctx.get_lastRet()));

            String processedCode = script.toString().replaceAll("^//.*", "");

            // 执行初始化代码
//            context.eval("js", "_last.data=_lastRet");

            // 定义并执行函数
            String functionCode = String.format("function fn(){ %s };fn();", processedCode);
            Value result = context.eval("js", functionCode);

            ctx.set_var(JSONObject.parse(ctx.get_var().toJSONString()));
            ctx.set_env(JSONObject.parse(ctx.get_env().toJSONString()));
            if (!ctx.get_par().isEmpty()) {
                var parJson = JSON.toJSONString(ctx.get_par());
                Map<String, Object> parClone = JSONObject.parse(parJson);
                ctx.set_par(parClone);
            }
//            var lastRetClone = JSON.toJSONString(ctx.get_lastRet());
//            if (JSON.isValid(lastRetClone)) {
//                ctx.set_lastRet(JSON.parse(lastRetClone));
//            }


            // 使用JSON转换确保线程安全
            Object funcRes = JSON.toJSON(result.as(Object.class));
            return new LogicItemRunResult().setData(funcRes);

        } catch (Exception e) {
            log.error("[{}]bizId:{},js function error: {}", ctx.getLogicId(), ctx.getBizId(), e.getMessage());
            e.printStackTrace();
            return new LogicItemRunResult()
                    .setMsg(e.getMessage())
                    .setSuccess(false);
        }
    }

    /**
     * 深度转换对象为纯 JSON 结构，确保 JS 可以访问嵌套属性
     * 通过先序列化再反序列化，将所有 Lombok 对象转换为 Map/List 结构
     */
    private Object deepConvertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            // 先序列化为 JSON 字符串，再解析为纯 Map/List 结构
            // 使用 Object.class 作为目标类型，fastjson2 会自动转换为 Map/List
            String jsonStr = JSON.toJSONString(obj);
            return JSON.parseObject(jsonStr, Object.class);
        } catch (Exception e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return obj;
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
