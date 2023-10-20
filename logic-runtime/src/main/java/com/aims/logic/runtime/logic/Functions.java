package com.aims.logic.runtime.logic;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.logic.functions.HttpFunction;
import com.aims.logic.runtime.logic.functions.JsFunction;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import kotlin.Function;
import kotlin.jvm.functions.Function2;
import okhttp3.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class Functions {
    static Map<String, Function2<FunctionContext, Object, Object>> functions = new HashMap<>();
//    static Map<String, Function<Object>> functions = new HashMap<>();


    static {
        functions.put("js",new JsFunction());
//        functions.put("js", (ctx, script) -> {
//            ScriptEngineManager manager = new ScriptEngineManager();
//            ScriptEngine engine = manager.getEngineByName("js");
//            engine.put("_var", ctx.get_var());
//            engine.put("_env", ctx.get_env());
//            engine.put("_par", ctx.get_par());
//            engine.put("_ret", ctx.get_ret());
//            engine.put("_lastRet", ctx.get_lastRet());
//            try {
//                engine.eval(String.format("function fn(){ %s }", script));
//                Invocable inv = (Invocable) engine;
//                return inv.invokeFunction("fn");
//            } catch (Exception exception) {
//                ctx.setHasErr(true);
//                ctx.setErrMsg(exception.toString());
//                System.out.println(exception.toString());
//                return null;
//            }
//        });
        functions.put("http", new HttpFunction());
//        functions.put("http", (ctx, item) -> {
//            var itemDsl = ((LogicItemTreeNode) item);
//            Object data = Functions.get("js").invoke(ctx, itemDsl.getBody());
//            var customHeaders = Functions.get("js").invoke(ctx, itemDsl.getHeaders());
//            var method = itemDsl.getMethod().isEmpty() ? "post" : itemDsl.getMethod();
//            var url = Functions.get("js").invoke(ctx, itemDsl.getUrl());
//            OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
//            Map<String, String> headerMap = new HashMap<>();
//            JSONObject cusHeadersJson = (JSONObject) JSON.toJSON(customHeaders);
//            if (cusHeadersJson != null) {
//                cusHeadersJson.forEach((k, v) -> {
//                    headerMap.put(k, (String) v);
//                });
//            }
//            String jsonData = data == null ? "{}" : JSON.toJSONString(data);
//            Headers headers = Headers.of(headerMap);
//            Request req;
//            var reqBuilder = new Request.Builder().url((String) url).headers(headers);
//            if (method.equalsIgnoreCase("get")) {
//                req = reqBuilder.get().build();
//            } else {
//                RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
//                req = reqBuilder.header("content-type", "application/json")
//                        .method(method, body).build();
//            }
//            System.out.println("-----http fn-----");
//            System.out.printf("%s:%s%n",method, url);
//            System.out.println(jsonData);
//            System.out.println(headers);
//            try {
//                Object repData = null;
//                try (var rep = client.newCall(req).execute()) {
//                    if (!rep.isSuccessful()) {
//                        ctx.setErrMsg(String.format("请求异常，Http Code:%s,%s", rep.code(), rep.message()));
//                        ctx.setHasErr(true);
//                    }
//                    if (rep.body() != null) {
//                        String repBody = rep.body().string();
//                        if (JSON.isValid(repBody))
//                            repData = JSON.parseObject(repBody);
//                        else repData = repBody;
//                    }
//                }
//                System.out.println(repData);
//                return repData;
//            } catch (IOException e) {
//                ctx.setHasErr(true);
//                ctx.setErrMsg(e.getLocalizedMessage());
//                return e.toString();
//            }
//        });
//
    }

    public Functions() {
//        functions.put("js", (ctx, script) -> {
//            ScriptEngineManager manager = new ScriptEngineManager();
//            ScriptEngine engine = manager.getEngineByName("js");
////        //动态声明对象参数，匿名类型
////        var v = new Object() {
////            public String a = "2000";
////        };
////        //转换为json对象传入，匿名类型不能直接传入，否则获取不到值
////        JSONObject j = (JSONObject) JSON.toJSON(v);
////        //声明整型变量，非引用类型，看是否会被js改变
////        var i = 1;
////        //将Java变量放入js上下文
//            engine.put("_var", ctx.get_var());
//            engine.put("_env", ctx.get_env());
//            engine.put("_par", ctx.get_par());
//            engine.put("_ret", ctx.get_ret());
//            try {
//                return (JSONObject) JSON.toJSON(engine.eval((String) script));
//            } catch (Exception exception) {
//                System.out.println(exception.toString());
//                return null;
//            }
////        engine.put("i", i);
////            //js内改变Java变量
////            var res = engine.eval("javaVar.a+=\"ddd\";i+=1;");
////            //js内调用看是否改变成功
////            res = engine.eval("print(javaVar.a,i)");
////            //通过函数的方式返回给Java变量，此处只是函数声明，返回为null
////            //想直接获取，可以直接写res=engine.eval("javaVar")
////            engine.eval("function d(){return {a:javaVar.a,i:i}}");
////            Invocable invocable = (Invocable) engine;
////            //调用js函数，获取返回值
////            var jres = (ScriptObjectMirror) invocable.invokeFunction("d");
////            System.out.println("获取js变更后的整型参数");
////            System.out.println(jres.get("i"));
////            System.out.println("java中的整型变量i");
////            System.out.println(i);//整型值未被js改变
////            var jvar = engine.get("javaVar");
////            System.out.println("获取js变更后的对象参数");
////            System.out.println(jvar);
////            System.out.println("java中的对象变量j");
////            System.out.println(j);//对象变量的属性被改变
//        });
//        functions.put("http", (ctx, item) -> {
//            var ret = new JSONObject();
//            var itemDsl = ((LogicItemTreeNode) item);
//            var data = Functions.get("js").invoke(ctx, itemDsl.getBody());
//            var customHeaders = Functions.get("js").invoke(ctx, itemDsl.getHeaders());
//            var method = itemDsl.getMethod().isEmpty() ? "post" : itemDsl.getMethod();
//            var url = Functions.get("js").invoke(ctx, itemDsl.getUrl());
//            OkHttpClient client = new OkHttpClient();
//            Map<String, String> headerMap = new HashMap<>();
//            JSONObject cusHeadersJson = (JSONObject) JSON.toJSON(customHeaders);
//            cusHeadersJson.forEach((k, v) -> {
//                headerMap.put(k, (String) v);
//            });
//            Headers headers = Headers.of(headerMap);
//            RequestBody body = RequestBody.create((String) data, MediaType.get("application/json"));
//            Request req = new Request.Builder().url((String) url).method(method, body).headers(headers).build();
//            System.out.println("-----http fn-----");
//            System.out.println(url);
//            System.out.println(method);
//            System.out.println(body);
//            System.out.println(headers);
//            client.newCall(req);
//            return ret;
//        });
    }

    public static Function2<FunctionContext, Object, Object> get(String name) {
        return functions.get(name);
    }
}
