package com.aims.logic.sdk.util;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.sdk.RuntimeEnvs;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.time.Duration;

public class RuntimeUtil {
    private static JSONObject ENVs;
    public static final String CONFIG_FILE_ROOT_DIR = "logic-configs";
    public static final String ENV_DIR = "logic-configs/envs";
    public static final String LOGIC_DIR = "logic-configs/logics";

    /**
     * 获取强类型的环境变量，主要用于系统变量的方便读取
     *
     * @return 返回强类型环境变量
     */
    public static RuntimeEnvs getEnv() {
        return getEnvJson().toJavaObject(RuntimeEnvs.class);
    }

    /**
     * 获取原始配置的环境变量json，包含自定义的配置
     *
     * @return 原始配置的环境变量json
     */
    public static JSONObject getEnvJson() {
        if (ENVs == null) {
            ENVs = readEnv();
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
        if (getEnv().getLOGIC_CONFIG_MODEL() == LogicConfigModelEnum.online) {
            OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(Duration.ofSeconds(10)).build();
            String url = String.format("%s/api/ide/logic/json/%s", getEnv().getIDE_HOST(), logicId);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (var rep = client.newCall(request).execute()) {
                if (rep.isSuccessful()) {
                    if (rep.body() != null) {
                        var res = rep.body().string();
                        if (JSON.isValid(res)) {
                            return JSON.parseObject(res);
                        }
                    }
                } else {
                    throw new RuntimeException(String.format("online获取配置失败，逻辑编号:%s,错误：%s,%s", logicId, rep.code(), rep.message()));
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("online获取配置失败，逻辑编号:%s,错误：%s", logicId, e.getLocalizedMessage()));
            }
        } else {
            return FileUtil.readJsonFile(LOGIC_DIR, logicId + ".json");
        }
        return null;
    }

    /**
     * 保存配置到本地
     *
     * @param logicId    逻辑编号
     * @param configJson 逻辑配置json字符串
     */
    public static void saveLogicConfigToFile(String logicId, String configJson) {
        try {
            FileUtil.writeFile(LOGIC_DIR, logicId + ".json", configJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取配置文件中的环境变量
     *
     * @return 环境变量json对象
     */
    public static JSONObject readEnv() {
        JSONObject envIdx = FileUtil.readJsonFile(ENV_DIR, "index.json");
        String _env = envIdx.get("env").toString();
        String envFileName = String.format("env.%s.json", _env);
        return FileUtil.readJsonFile(ENV_DIR, envFileName);
    }
}
