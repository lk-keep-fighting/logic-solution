package com.aims.logic.sdk.util;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.sdk.RuntimeEnvs;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;

public class RuntimeUtil {
    private static JSONObject ENVs;
    public static final String ENV_DIR = "envs";
    public static final String LOGIC_DIR = "logics";

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
     * 获取当前启动项目的url
     *
     * @return http://ip:port
     */
    public static String getUrl() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String url = String.format("http://%s:%s", request.getServerName(), request.getServerPort());
        System.out.println("读取本机Host:" + url);
        return url;
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
            String url = String.format("%s/api/ide/logic/%s/config", getEnv().getIDE_HOST().isBlank() ? getUrl() : getEnv().getIDE_HOST(), logicId);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (var rep = client.newCall(request).execute()) {
                if (rep.isSuccessful()) {
                    if (rep.body() != null) {
                        var res = rep.body().string();
                        if (JSON.isValid(res)) {
                            var json = JSON.parseObject(res);
                            return json.getJSONObject("data");
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
    public static String saveLogicConfigToFile(String logicId, String configJson) {
        try {
            return FileUtil.writeFile(LOGIC_DIR, logicId + ".json", configJson);
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
        JSONObject envIdx = FileUtil.readOrCreateFile(ENV_DIR, "index.json", "{\"env\":\"dev\"}");
        String _env = envIdx.get("env").toString();
        String envFileName = String.format("env.%s.json", _env);
        String defEnvFile = "{\"NODE_ENV\":\"" + _env + "\",\"LOGIC_CONFIG_MODEL\":\"online\",\"IDE_HOST\":\"\",\"JWT\":{},\"LOG\":\"error\"}";
        return FileUtil.readOrCreateFile(ENV_DIR, envFileName, defEnvFile);
    }
}
