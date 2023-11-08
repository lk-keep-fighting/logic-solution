package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.HttpFunctionService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liukun
 */
@Service
public class HttpFunction implements HttpFunctionService {
    public HttpFunction() {
    }

    OkHttpClient client = new OkHttpClient();

    @Override
    public Object invoke(FunctionContext ctx, Object item) {
        var itemDsl = ((LogicItemTreeNode) item);
        Object data = Functions.get("js").invoke(ctx, itemDsl.getBody());
        var customHeaders = Functions.get("js").invoke(ctx, itemDsl.getHeaders());
        var method = itemDsl.getMethod().isEmpty() ? "post" : itemDsl.getMethod();
        var url = Functions.get("js").invoke(ctx, itemDsl.getUrl());
        client.newBuilder().connectTimeout(Duration.ofMillis(Long.parseLong(itemDsl.getTimeout()))).build();
        Map<String, String> headerMap = new HashMap<>();
        JSONObject cusHeadersJson = (JSONObject) JSON.toJSON(customHeaders);
        if (cusHeadersJson != null) {
            cusHeadersJson.forEach((k, v) -> headerMap.put(k, (String) v));
        }
        String jsonData = data == null ? "{}" : JSON.toJSONString(data);
        Headers headers = Headers.of(headerMap);
        Request req;
        var reqBuilder = new Request.Builder().url((String) url).headers(headers);
        if ("get".equalsIgnoreCase(method)) {
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
                    if (JSON.isValid(repBody)) {
                        repData = JSON.parseObject(repBody);
                    } else {
                        repData = repBody;
                    }
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

    @Override
    public String getItemType() {
        return "http";
    }
}
