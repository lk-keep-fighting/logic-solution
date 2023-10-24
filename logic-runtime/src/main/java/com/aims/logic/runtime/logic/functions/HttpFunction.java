package com.aims.logic.runtime.logic.functions;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.logic.FunctionContext;
import com.aims.logic.runtime.logic.Functions;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class HttpFunction implements IFunction {

    @Override
    public Object invoke(FunctionContext ctx, Object item) {
        var itemDsl = ((LogicItemTreeNode) item);
        Object data = Functions.get("js").invoke(ctx, itemDsl.getBody());
        var customHeaders = Functions.get("js").invoke(ctx, itemDsl.getHeaders());
        var method = itemDsl.getMethod().isEmpty() ? "post" : itemDsl.getMethod();
        var url = Functions.get("js").invoke(ctx, itemDsl.getUrl());
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        Map<String, String> headerMap = new HashMap<>();
        JSONObject cusHeadersJson = (JSONObject) JSON.toJSON(customHeaders);
        if (cusHeadersJson != null) {
            cusHeadersJson.forEach((k, v) -> {
                headerMap.put(k, (String) v);
            });
        }
        String jsonData = data == null ? "{}" : JSON.toJSONString(data);
        Headers headers = Headers.of(headerMap);
        Request req;
        var reqBuilder = new Request.Builder().url((String) url).headers(headers);
        if (method.equalsIgnoreCase("get")) {
            req = reqBuilder.get().build();
        } else {
            RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
            req = reqBuilder.header("content-type", "application/json")
                    .method(method, body).build();
        }
        System.out.println("-----http fn-----");
        System.out.printf("%s:%s%n", method, url);
        System.out.println(jsonData);
        System.out.println(headers);
        try {
            Object repData = null;
            try (var rep = client.newCall(req).execute()) {
                if (!rep.isSuccessful()) {
                    ctx.setErrMsg(String.format("请求异常，Http Code:%s,%s", rep.code(), rep.message()));
                    ctx.setHasErr(true);
                }
                if (rep.body() != null) {
                    String repBody = rep.body().string();
                    if (JSON.isValid(repBody))
                        repData = JSON.parseObject(repBody);
                    else repData = repBody;
                }
            }
            System.out.println(repData);
            return repData;
        } catch (IOException e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.getLocalizedMessage());
            return e.toString();
        }
    }
}
