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
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liukun
 */
@Slf4j
@Component
public class LogicConfigStoreServiceImpl implements LogicConfigStoreService {
    OkHttpClient httpClient;

    public LogicConfigStoreServiceImpl() {
        httpClient = new OkHttpClient().newBuilder().callTimeout(Duration.ofSeconds(10)).build();
    }

    public static Cache<String, JSONObject> logicConfigCache = Caffeine.newBuilder().initialCapacity(100)
            //最大容量为200
//                .maximumSize(200)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    public Cache<String, JSONObject> getLogicConfigCache() {
        return logicConfigCache;
    }

    public JSONObject readFromCache(String logicId, String version) {
        JSONObject logicConfig = null;
        String logicCacheKey = logicId + "-" + version;
        logicConfig = logicConfigCache.asMap().get(logicCacheKey);
        if (logicConfig != null) {
            log.info("从缓存读取逻辑配置[{}]，json内version：{}", logicCacheKey, logicConfig.get("version"));
            return logicConfig;
        }
        log.info("从缓存读取逻辑配置[{}]，version：{}，***未命中***", logicCacheKey, version);
        return null;
    }

    public void removeFromCache(String logicId, String version) {
        String logicCacheKey = logicId + "-" + version;
        logicConfigCache.asMap().remove(logicCacheKey);
        log.info("从缓存移除逻辑配置[{}]", logicCacheKey);
    }

    public JSONObject saveToCache(String logicId, String version, JSONObject logicConfig) {
        String logicCacheKey = logicId + "-" + version;
        logicConfig.put("id", logicId);//自动修复文件名编号与内部配置编号不同的问题
        if (logicConfig.get("visualConfig") != null) {
            logicConfig.remove("visualConfig");//删除可视化配置减少缓存
        }
        logicConfigCache.put(logicCacheKey, logicConfig);
        log.info("将逻辑配置[{}]写入缓存，json内version：{}", logicCacheKey, logicConfig.get("version"));
        return logicConfig;
    }

    @Override
    public JSONObject readLogicConfig(String logicId, String version, LogicConfigModelEnum logicConfigModel) {
        JSONObject logicConfig = null;
        //当指定offline时，始终从本地文件读取，否则默认为online
        if (logicConfigModel == LogicConfigModelEnum.offline) {
            version = "offline";
            logicConfig = readFromCache(logicId, version);
            if (logicConfig != null) {
                return logicConfig;
            }
            logicConfig = readLogicConfigFromFile(logicId);
            if (logicConfig != null) {
                return saveToCache(logicId, version, logicConfig);
            } else {
                throw new RuntimeException(String.format("配置模式：offline，未找到【%s】的配置", logicId));
            }
        } else {
            if (version != null) {//版本不为null，先尝试从缓存读取
                logicConfig = readFromCache(logicId, version);
                if (logicConfig != null) {
                    return logicConfig;
                }
            }
            logicConfig = readLogicConfigFromHost(logicId, version);
            if (logicConfig != null) {
                return saveToCache(logicId, version, logicConfig);
            } else {
                throw new RuntimeException(String.format("配置模式：online，未找到【%s】的配置", logicId));
            }
        }
    }

    @Override
    public JSONObject readLogicConfigFromFile(String logicId) {
        try {
            return FileUtil.readJsonFile(FileUtil.LOGIC_DIR, logicId + ".json");
        } catch (Exception e) {
            log.error("读取逻辑配置文件[{}.json]报错", logicId);
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getOfflineLogicIds() {
        List<String> logicFiles = FileUtil.getFileList(FileUtil.LOGIC_DIR);
        return logicFiles.stream().map(s -> s.replace(".json", "")).collect(Collectors.toList());
    }

    @Override
    public JSONObject readLogicConfigFromHost(String logicId, String version) {
        String onlineHost = RuntimeUtil.getOnlineHost();//.getEnvObject().getIDE_HOST().isBlank() ? RuntimeUtil.getUrl() : RuntimeUtil.getEnvObject().getIDE_HOST();
        String url;
        if (version == null) {//读取最新配置
            url = String.format("%s/api/ide/logic/%s/config", onlineHost, logicId);
            log.info("online-从[{}]读取最新配置logicId:[{}]", url, logicId);
        } else {
            url = String.format("%s/api/ide/logic/%s/config/%s", onlineHost, logicId, version);
            log.info("online-从[{}]读取指定版本配置logicId:[{}]-version:[{}]", url, logicId, version);
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        JSONObject logicConfig = null;
        try (var rep = httpClient.newCall(request).execute()) {
            if (rep.isSuccessful()) {
                if (rep.body() != null) {
                    var res = rep.body().string();
                    if (JSON.isValid(res)) {
                        var json = JSON.parseObject(res);
                        logicConfig = json.getJSONObject("data");
                        if (logicConfig == null)
                            throw new RuntimeException(String.format("%s的配置在%s中不存在", logicId, url));
                    }
                }
            } else {
                throw new RuntimeException(String.format("请求地址%s获取配置失败，逻辑编号:%s,错误：%s,%s", url, logicId, rep.code(), rep.message()));
            }
        } catch (Exception e) {
            log.error("请求地址{}获取配置异常，逻辑编号:{}，错误：{}", url, logicId, e.getLocalizedMessage());
            throw new RuntimeException(String.format("online获取配置失败，逻辑编号:%s,错误：%s", logicId, e.getLocalizedMessage()));
        }
        return logicConfig;
    }


    @Override
    public String saveLogicConfigToFile(String logicId, String configJson) {
        try {
            String path = FileUtil.writeFile(FileUtil.LOGIC_DIR, logicId + ".json", configJson);
            log.info("已保存逻辑配置到文件[{}.json]", logicId);
            return path;
        } catch (Exception e) {
            log.error("保存逻辑配置报错[{}.json]", logicId);
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
