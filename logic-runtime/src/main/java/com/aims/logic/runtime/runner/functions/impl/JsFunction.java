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

            // 设置变量到JavaScript上下文中，使用JSON转换确保可访问性
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

//    // 深度解包GraalVM值（核心解决方法）
//    private Object deepUnwrapGraalValue(Value value) {
//        if (value == null) return null;
//
//        // 处理原始类型
//        if (value.isString()) return value.asString();
//        if (value.isBoolean()) return value.asBoolean();
//        if (value.isNumber()) return value.asDouble();
//        if (value.isNull()) return null;
//
//        // 处理数组
//        if (value.hasArrayElements()) {
//            List<Object> list = new ArrayList<>();
//            long size = value.getArraySize();
//            for (long i = 0; i < size; i++) {
//                list.add(deepUnwrapGraalValue(value.getArrayElement(i)));
//            }
//            return list;
//        }
//        // 处理对象
//        if (value.hasMembers()) {
//            Map<String, Object> map = new HashMap<>();
//            for (String key : value.getMemberKeys()) {
//                Value member = value.getMember(key);
//
//                // 跳过函数和特殊对象
//                if (!member.canExecute() && !member.isHostObject()) {
//                    map.put(key, deepUnwrapGraalValue(member));
//                }
//            }
//            return map;
//        }
//
//        // 回退：尝试作为Java对象获取
//        try {
//            return value.as(Object.class);
//        } catch (Exception e) {
//            return null;
//        }
//    }

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
