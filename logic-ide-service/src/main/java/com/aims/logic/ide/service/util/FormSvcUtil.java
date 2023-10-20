package com.aims.logic.ide.service.util;

import com.aims.logic.ide.service.util.dto.FormQueryInput;
import com.aims.logic.ide.service.util.dto.FormQueryReturn;
import com.aims.logic.runtime.RuntimeEnvs;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.time.Duration;

public class FormSvcUtil {
    public static FormQueryReturn query(String formName, FormQueryInput input) {
        JSONObject env = RuntimeUtil.readEnv();
        RuntimeEnvs runtimeEnvs = env.to(RuntimeEnvs.class);
        OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(Duration.ofSeconds(10)).build();
        RequestBody body = RequestBody.create(JSON.toJSONString(input), MediaType.parse("application/json; charset=utf-8"));
        String url = String.format("%s/api/form/%s/query", runtimeEnvs.getFORM_HOST(), formName);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .build();
        try (var rep = client.newCall(request).execute()) {
            if (rep.body() != null) {
                var res = rep.body().string();
                if (JSON.isValid(res)) {
                    return JSON.parseObject(res, FormQueryReturn.class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
