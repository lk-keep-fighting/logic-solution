package com.aims.logic.runtime.store.impl;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.aims.logic.runtime.util.FileUtil;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.time.Duration;

/**
 * @author liukun
 */
public class LogicConfigStoreServiceImpl implements LogicConfigStoreService {
    OkHttpClient httpClient = new OkHttpClient();

    @Override
    public JSONObject readLogicConfig(String logicId, String version) {
        JSONObject logicConfig = null;
        //当指定offline时，始终从本地文件读取，否则默认为online
        if (RuntimeUtil.getEnvObject().getLOGIC_CONFIG_MODEL() == LogicConfigModelEnum.offline) {
            logicConfig = FileUtil.readJsonFile(FileUtil.LOGIC_DIR, logicId + ".json");
        } else {
            OkHttpClient client = httpClient.newBuilder().callTimeout(Duration.ofSeconds(10)).build();
            String onlineHost = RuntimeUtil.getEnvObject().getIDE_HOST().isBlank() ? RuntimeUtil.getUrl() : RuntimeUtil.getEnvObject().getIDE_HOST();
            String url;
            if (version == null) {//读取最新配置
                url = String.format("%s/api/ide/logic/%s/config", onlineHost, logicId);
            } else {
                url = String.format("%s/api/ide/logic/%s/config/%s", onlineHost, logicId, version);
            }
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (var rep = client.newCall(request).execute()) {
                if (rep.isSuccessful()) {
                    if (rep.body() != null) {
                        var res = rep.body().string();
                        if (JSON.isValid(res)) {
                            var json = JSON.parseObject(res);
                            logicConfig = json.getJSONObject("data");
                        }
                    }
                } else {
                    throw new RuntimeException(String.format("online获取配置失败，逻辑编号:%s,错误：%s,%s", logicId, rep.code(), rep.message()));
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("online获取配置失败，逻辑编号:%s,错误：%s", logicId, e.getLocalizedMessage()));
            }
        }
        if (logicConfig != null) {
            logicConfig.put("id", logicId);//自动修复文件名编号与内部配置编号不同的问题
        }
        return logicConfig;
    }

    @Override
    public String saveLogicConfigToFile(String logicId, String configJson) {
        try {
            return FileUtil.writeFile(FileUtil.LOGIC_DIR, logicId + ".json", configJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
