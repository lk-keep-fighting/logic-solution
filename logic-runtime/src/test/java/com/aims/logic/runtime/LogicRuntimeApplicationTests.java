package com.aims.logic.runtime;

import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.functions.impl.JsFunction;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

@SpringBootTest(classes = LogicRuntimeApplicationTests.class)
//@ContextConfiguration
class LogicRuntimeApplicationTests {
    @Test
    void contextLoads() {
        JsFunction js = new JsFunction();
        JSONObject j = new JSONObject();
        j.putArray("ways");
        var ctx = new FunctionContext();
        ctx.set_var(j);
        var res = js.invoke(ctx, "return _var.ways;");
        System.out.println(res);
//        js.invoke(ctx, "return _var.ways.push({})");
    }

//    @Test
    void testJsArray() {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine();

        int[] arr = {};
        try {
            engine.put("array", arr);
            engine.eval("array.push(2);");

            // 获取修改后的数组
            int[] modifiedArr = (int[]) engine.get("array");
            for (int i : modifiedArr) {
                System.out.println(i); // 输出：1, 2, 3
            }

        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
