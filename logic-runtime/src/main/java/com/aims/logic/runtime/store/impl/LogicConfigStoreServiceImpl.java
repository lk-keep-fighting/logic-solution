package com.aims.logic.runtime.store.impl;

import com.aims.logic.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.aims.logic.util.FileUtil;
import com.aims.logic.util.RuntimeUtil;
import com.aims.logic.util.SpringContextUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * @author liukun
 */
public class LogicConfigStoreServiceImpl implements LogicConfigStoreService {
    @Override
    public JSONObject readLogicConfig(String logicId) {
        if (RuntimeUtil.getEnv().getLOGIC_CONFIG_MODEL() == LogicConfigModelEnum.online) {
            OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(Duration.ofSeconds(10)).build();
            String url = String.format("%s/api/ide/logic/%s/config", RuntimeUtil.getEnv().getIDE_HOST().isBlank() ? RuntimeUtil.getUrl() : RuntimeUtil.getEnv().getIDE_HOST(), logicId);
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
            return FileUtil.readJsonFile(FileUtil.LOGIC_DIR, logicId + ".json");
        }
        return null;
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
