package com.aims.logic.runtime.logic.functions;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.logic.FunctionContext;
import com.aims.logic.runtime.logic.Functions;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import kotlin.jvm.functions.Function2;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class JavaMethodFunction implements Function2<FunctionContext, Object, Object> {

    @Override
    public Object invoke(FunctionContext ctx, Object item) {
        var itemDsl = ((LogicItemTreeNode) item);
        Object data = Functions.get("js").invoke(ctx, itemDsl.getBody());
        var method = itemDsl.getMethod().isEmpty() ? "post" : itemDsl.getMethod();
        String jsonData = data == null ? "{}" : JSON.toJSONString(data);

        System.out.println("-----java method fn-----");
        System.out.println(jsonData);
        try {
            Object repData = null;
//            try (var rep = client.newCall(req).execute()) {
//                if (!rep.isSuccessful()) {
//                    ctx.setErrMsg(String.format("请求异常，Http Code:%s,%s", rep.code(), rep.message()));
//                    ctx.setHasErr(true);
//                }
//                if (rep.body() != null) {
//                    String repBody = rep.body().string();
//                    if (JSON.isValid(repBody))
//                        repData = JSON.parseObject(repBody);
//                    else repData = repBody;
//                }
//            }
            System.out.println(repData);
            return repData;
        } catch (Exception e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.getLocalizedMessage());
            return e.toString();
        }
    }
}
