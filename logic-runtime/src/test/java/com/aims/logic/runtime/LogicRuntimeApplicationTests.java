package com.aims.logic.runtime;

import com.aims.logic.runtime.logic.FunctionContext;
import com.aims.logic.runtime.logic.functions.JsFunction;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@SpringBootTest
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

    @Test
    void testJsArray() {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine();

        int[] arr = {};
        try {
            engine.put("array", arr);
            engine.eval("array.append(2); array.append(3);");

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
