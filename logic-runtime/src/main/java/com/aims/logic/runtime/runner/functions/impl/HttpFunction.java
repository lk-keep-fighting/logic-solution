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
        var itemRes = new LogicItemRunResult();
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
        itemDsl.setMethod(method);
        itemDsl.setHeaders(JSONObject.from(headerMap).toJSONString());
        itemDsl.setBody(jsonData);
        itemDsl.setUrl(url);
        itemDsl.setTimeout(itemDsl.getTimeout());
        Request req;
        var reqBuilder = new Request.Builder().url(url).headers(headers);
        if ("get".equalsIgnoreCase(method)) {
            req = reqBuilder.get().build();
        } else {
            RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
            req = reqBuilder
                    .method(method, body).build();
        }
        log.debug("[{}]bizId:{},>>http fn,method:{},url:{},data:{},headers:{}", ctx.getLogicId(), ctx.getBizId(), method, url, jsonData, headers);
        try {
            Object repData = null;
            try (var rep = client.newCall(req).execute()) {
                if (!rep.isSuccessful()) {
                    itemRes.setMsg(String.format("请求异常，Http Code:%s,%s", rep.code(), rep.message()));
                    itemRes.setSuccess(false);
                    log.error("[{}]bizId:{},>>http 请求异常,rep code:{},rep msg:{}", ctx.getLogicId(), ctx.getBizId(), rep.code(), rep.message());
                }
                if (rep.body() != null) {
                    String repBody = rep.body().string();
                    if (JSON.isValid(repBody)) {
                        repData = JSON.parseObject(repBody);
                    } else {
                        repData = repBody;
                    }
                }
            } catch (IOException e) {
                var msg = String.format("[%s]bizId:%s,>>http IOException,msg:%s", ctx.getLogicId(), ctx.getBizId(), e.getLocalizedMessage());
                log.error(msg);
                return itemRes.setSuccess(false).setMsg(msg)
                        .setItemInstance(itemDsl)
                        .setData(e.toString());
            }
            return itemRes
                    .setItemInstance(itemDsl).setData(repData);
        } catch (Exception e) {
            var msg = String.format("[%s]bizId:%s,>>http意外的异常,msg:%s", ctx.getLogicId(), ctx.getBizId(), e.getLocalizedMessage());
            log.error(msg);
            return itemRes.setSuccess(false).setMsg(msg)
                    .setItemInstance(itemDsl)
                    .setData(e.toString());
        }
    }

    @Override
    public String getItemType() {
        return "http";
    }

    @Override
    public int getPriority(String env) {
        return 0;
    }
}
