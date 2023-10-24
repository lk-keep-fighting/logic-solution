package com.aims.logic.sdk.util;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.sdk.RuntimeEnvs;
import com.aims.logic.util.JsonUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.time.Duration;

public class RuntimeUtil {
    private static RuntimeEnvs ENVs;

    public static RuntimeEnvs GetEnv() {
        if (ENVs == null) {
            ENVs = readEnv().toJavaObject(RuntimeEnvs.class);
        }
        return ENVs;
    }

    /**
     * 根据逻辑编号读取逻辑配置
     *
     * @param logicId 逻辑编号
     * @return 逻辑配置
     */
    public static JSONObject readLogicConfig(String logicId) {
        if (GetEnv().getLOGIC_CONFIG_MODEL() == LogicConfigModelEnum.online) {
            OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(Duration.ofSeconds(10)).build();
            String url = String.format("%s/api/ide/asset/logic/%s", GetEnv().getIDE_HOST(), logicId);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (var rep = client.newCall(request).execute()) {
                if (rep.body() != null) {
                    var res = rep.body().string();
                    if (JSON.isValid(res)) {
                        return JSON.parseObject(res);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String path = String.format("/static/logics/%s.json", logicId);
            return JsonUtil.readJsonFile(path);
        }
        return null;
    }

    /**
     * 读取配置文件中的环境变量
     *
     * @return 环境变量json对象
     */
    public static JSONObject readEnv() {
        String envIdxPath = "/static/envs/index.json";
        JSONObject envIdx = JsonUtil.readJsonFile(envIdxPath);
        String _env = envIdx.get("env").toString();
        String envPath = String.format("/static/envs/env.%s.json", _env);
        return JsonUtil.readJsonFile(envPath);
    }
}
