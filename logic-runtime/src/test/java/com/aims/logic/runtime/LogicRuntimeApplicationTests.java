package com.aims.logic.runtime;

import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.LogicRunner;
import com.aims.logic.runtime.runner.functions.impl.JsFunction;
import com.alibaba.fastjson2.JSONObject;
import org.graalvm.polyglot.Engine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

//@SpringBootTest(classes = LogicRuntimeApplicationTests.class)
@ContextConfiguration()
class LogicRuntimeApplicationTests {
    //    @Test
    void javaCode() {
        var config = JSONObject.parse("{\"id\":\"test.java\",\"name\":null,\"schemaVersion\":null,\"version\":\"20231212215153\",\"label\":null,\"description\":null,\"triggerType\":null,\"cron\":null,\"typeParams\":null,\"params\":null,\"returns\":null,\"variables\":null,\"envs\":null,\"items\":[{\"type\":\"start\",\"id\":\"06577d00-2fde-482f-b21c-894b0861edf1\",\"name\":\"start\",\"nextId\":\"a1e5c8c9-d145-45a9-9262-dadd731dd8a9\"},{\"type\":\"java\",\"name\":\"java代码块\",\"system\":\"\",\"url\":\"'com.aims.logic.sdk.test'\",\"method\":\"'func1'\",\"body\":\"return {\\n    \\\"ctx\\\": {},\\n    \\\"script\\\": \\\"return js;\\\"\\n}\",\"timeout\":5000,\"id\":\"a1e5c8c9-d145-45a9-9262-dadd731dd8a9\"}],\"visualConfig\":{\"cells\":[{\"shape\":\"edge\",\"id\":\"77c2a20c-010f-4dce-ba1f-3b1a70a0aa05\",\"zIndex\":0,\"source\":{\"cell\":\"06577d00-2fde-482f-b21c-894b0861edf1\",\"port\":\"0b9732fa-955e-4f3c-bb8c-012d0a54255c\"},\"target\":{\"cell\":\"a1e5c8c9-d145-45a9-9262-dadd731dd8a9\",\"port\":\"f1a74e53-9343-4fa1-ae59-540ccb1671e5\"}},{\"position\":{\"x\":375,\"y\":75},\"size\":{\"width\":50,\"height\":50},\"attrs\":{\"text\":{\"text\":\"start\"}},\"visible\":true,\"shape\":\"circle\",\"id\":\"06577d00-2fde-482f-b21c-894b0861edf1\",\"data\":{\"config\":{\"type\":\"start\"}},\"zIndex\":1,\"ports\":{\"groups\":{\"bottom\":{\"position\":\"bottom\",\"attrs\":{\"circle\":{\"r\":4,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}},\"zIndex\":1}},\"items\":[{\"group\":\"bottom\",\"id\":\"0b9732fa-955e-4f3c-bb8c-012d0a54255c\"}]}},{\"position\":{\"x\":350,\"y\":185},\"size\":{\"width\":100,\"height\":50},\"attrs\":{\"image\":{\"width\":15,\"x\":2,\"y\":2,\"xlink:href\":\"/icons/code.svg\"},\"text\":{\"fontSize\":14,\"text\":\"java代码块\"}},\"shape\":\"ExtSharp\",\"id\":\"a1e5c8c9-d145-45a9-9262-dadd731dd8a9\",\"data\":{\"config\":{\"type\":\"java\",\"name\":\"java代码块\",\"system\":\"\",\"url\":\"'com.aims.logic.test'\",\"method\":\"'func1'\",\"body\":\"return {\\n    \\\"ctx\\\": {},\\n    \\\"script\\\": \\\"return js;\\\"\\n}\",\"timeout\":5000}},\"groups\":[\"biz\"],\"zIndex\":2,\"ports\":{\"groups\":{\"top\":{\"position\":\"top\",\"attrs\":{\"circle\":{\"r\":4,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"right\":{\"position\":\"right\",\"attrs\":{\"circle\":{\"r\":4,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}},\"zIndex\":1},\"bottom\":{\"position\":\"bottom\",\"attrs\":{\"circle\":{\"r\":4,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"left\":{\"position\":\"left\",\"attrs\":{\"circle\":{\"r\":4,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}}},\"items\":[{\"group\":\"top\",\"id\":\"f1a74e53-9343-4fa1-ae59-540ccb1671e5\"},{\"group\":\"right\",\"id\":\"8c6a7a41-46a9-43c7-9d94-7558f8f469ed\"},{\"group\":\"bottom\",\"id\":\"bf244176-b4c1-430b-9af9-d115aeb8515e\"},{\"group\":\"left\",\"id\":\"bb838f4e-0c79-4068-9d10-148110857bca\"}]}}]}}");

        LogicRunner runner = new LogicRunner(config, new JSONObject(), new JSONObject());
        var res = runner.run(null);
        System.out.println(res);
    }

    @Autowired
    Engine engine;

    @Test
    void contextLoads() {
        JsFunction js = new JsFunction(engine);
        JSONObject j = new JSONObject();
        j.putArray("ways");
        var ctx = new FunctionContext();
        ctx.set_var(j);
        var res = js.invoke(ctx, "_var.ways=[1,2];return _var.ways;");
        System.out.println(res.getData());
//        js.invoke(ctx, "return _var.ways.push({})");
    }

}
