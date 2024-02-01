package com.aims.logic.runtime.store.impl;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.aims.logic.runtime.util.FileUtil;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;

/**
 * @author liukun
 */
@Slf4j
@Service
public class LogicConfigStoreServiceImpl implements LogicConfigStoreService {
    OkHttpClient httpClient = new OkHttpClient();
    private Cache<String, JSONObject> logicConfigCache;

    public LogicConfigStoreServiceImpl() {
        logicConfigCache = Caffeine.newBuilder().initialCapacity(100)
                //最大容量为200
//                .maximumSize(200)
                .expireAfterAccess(Duration.ofDays(7))
                .build();
    }


    @Override
    public JSONObject readLogicConfig(String logicId, String version) {
        JSONObject logicConfig = null;
        //当指定offline时，始终从本地文件读取，否则默认为online
        if (RuntimeUtil.getEnvObject().getLOGIC_CONFIG_MODEL() == LogicConfigModelEnum.offline) {
            String logicCacheKey = logicId + "-" + version;
            logicConfig = logicConfigCache.asMap().get(logicCacheKey);
            if (logicConfig != null) {
                log.info("offline-从缓存读取逻辑配置[{}]，json内version：{}", logicCacheKey, logicConfig.get("version"));
                return logicConfig;
            }
            logicConfig = FileUtil.readJsonFile(FileUtil.LOGIC_DIR, logicId + ".json");
            if (logicConfig != null) {
                logicConfig.put("id", logicId);//修复复制配置时id可能不一致问题
                logicConfigCache.put(logicCacheKey, logicConfig);
                log.info("offline-从文件读取逻辑配置并更新到缓存[{}]，json内version：{}", logicCacheKey, logicConfig.get("version"));
                return logicConfig;
            }
        } else {
            OkHttpClient client = httpClient.newBuilder().callTimeout(Duration.ofSeconds(10)).build();
            String onlineHost = RuntimeUtil.getEnvObject().getIDE_HOST().isBlank() ? RuntimeUtil.getUrl() : RuntimeUtil.getEnvObject().getIDE_HOST();
            String url;
            if (version == null) {//读取最新配置
                url = String.format("%s/api/ide/logic/%s/config", onlineHost, logicId);
                log.info("online-从[{}]读取最新配置logicId:[{}]", url, logicId);
            } else {
                url = String.format("%s/api/ide/logic/%s/config/%s", onlineHost, logicId, version);
                log.info("online-从[{}]读取配置logicId:[{}]-version:[{}]", url, logicId, version);
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
                    log.error("online获取配置失败，逻辑编号:{}，错误：{},{}", logicId, rep.code(), rep.message());
                    throw new RuntimeException(String.format("online获取配置失败，逻辑编号:%s,错误：%s,%s", logicId, rep.code(), rep.message()));
                }
            } catch (IOException e) {
                log.error("online获取配置失败，逻辑编号:{}，错误：{}", logicId, e.getLocalizedMessage());
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
            String path = FileUtil.writeFile(FileUtil.LOGIC_DIR, logicId + ".json", configJson);
            log.info("已保存逻辑配置到文件[{}.json]", logicId);
            logicConfigCache.asMap().remove(logicId + "-null");
            log.info("已删除key为[{}-null]的缓存", logicId);
            return path;
        } catch (Exception e) {
            log.error("保存逻辑配置报错[{}.json]", logicId);
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
