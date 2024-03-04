package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liukun
 */
@Slf4j
@Service
public class HttpFunction implements ILogicItemFunctionRunner {
    public HttpFunction() {
    }

    OkHttpClient httpClient = new OkHttpClient();

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        var itemDsl = ((LogicItemTreeNode) item);
        var itemInstance = new LogicItemTreeNode();
        Object data = Functions.runJsByContext(ctx, itemDsl.getBody());
        var customHeaders = Functions.runJsByContext(ctx, itemDsl.getHeaders());
        var method = itemDsl.getMethod().isEmpty() ? "post" : itemDsl.getMethod();
        var url = (String) Functions.runJsByContext(ctx, itemDsl.getUrl());
        if (url != null && url.startsWith(("/"))) {
            url = RuntimeUtil.getUrl() + url;
        }
        OkHttpClient client = httpClient.newBuilder()
                .connectTimeout(Integer.parseInt(itemDsl.getTimeout()), TimeUnit.MILLISECONDS)
                .readTimeout(Integer.parseInt(itemDsl.getTimeout()), TimeUnit.MILLISECONDS)
                .writeTimeout(Integer.parseInt(itemDsl.getTimeout()), TimeUnit.MILLISECONDS)
                .build();
        Map<String, String> headerMap = new HashMap<>();
        if (customHeaders != null) {
            JSONObject cusHeadersJson = (JSONObject) JSON.toJSON(customHeaders);
            if (cusHeadersJson != null) {
                cusHeadersJson.forEach((k, v) -> headerMap.put(k, (String) v));
            }
        }

        String jsonData = data == null ? "{}" : JSON.toJSONString(data);
        if (!headerMap.containsKey("content-type")) {//默认json请求
            headerMap.put("content-type", "application/json");
        }
        Headers headers = Headers.of(headerMap);
        itemInstance.setMethod(method);
        itemInstance.setHeaders(JSONObject.from(headerMap).toJSONString());
        itemInstance.setBody(jsonData);
        itemInstance.setUrl(url);
        itemInstance.setTimeout(itemDsl.getTimeout());
        Request req;
        var reqBuilder = new Request.Builder().url(url).headers(headers);
        if ("get".equalsIgnoreCase(method)) {
            req = reqBuilder.get().build();
        } else {
            RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
            req = reqBuilder
                    .method(method, body).build();
        }
        log.debug("bizId:{},>>http fn,method:{},url:{},data:{},headers:{}", ctx.getBizId(), method, url, jsonData, headers);
        try {
            Object repData = null;
            try (var rep = client.newCall(req).execute()) {
                if (!rep.isSuccessful()) {
                    ctx.setErrMsg(String.format("请求异常，Http Code:%s,%s", rep.code(), rep.message()));
                    ctx.setHasErr(true);
                    log.error("bizId:{},>>http 请求异常,rep code:{},rep msg:{}", ctx.getBizId(), rep.code(), rep.message());
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
            return new LogicItemRunResult()
                    .setItemInstance(itemInstance).setData(repData);
        } catch (IOException e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.getLocalizedMessage());
            return new LogicItemRunResult()
                    .setItemInstance(itemInstance)
                    .setData(e.toString()).setMsg(e.toString());
        }
    }

    @Override
    public String getItemType() {
        return "http";
    }
}
