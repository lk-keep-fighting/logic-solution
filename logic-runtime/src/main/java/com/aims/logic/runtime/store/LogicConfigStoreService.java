package com.aims.logic.runtime.store;

import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.List;

/**
 * @author liukun
 */

public interface LogicConfigStoreService {
    Cache<String, JSONObject> getLogicConfigCache();

    /**
     * 根据逻辑编号读取逻辑配置
     *
     * @param logicId 逻辑编号
     * @return 逻辑配置
     */
    JSONObject readLogicConfig(String logicId, String version, LogicConfigModelEnum model);

    JSONObject readLogicConfigFromFile(String logicId);

    /**
     * 获取离线逻辑编号列表
     *
     * @return
     */
    List<String> getOfflineLogicIds();

    JSONObject readLogicConfigFromHost(String logicId, String version);

    /**
     * 保存配置到本地
     *
     * @param logicId    逻辑编号
     * @param configJson 逻辑配置json字符串
     */
    String saveLogicConfigToFile(String logicId, String configJson);

    /**
     * 从缓存中读取逻辑配置
     *
     * @param logicId 逻辑编号
     * @return 逻辑配置
     */
    JSONObject readFromCache(String logicId, String version);
    /**
     * 从缓存中删除逻辑配置
     *
     * @param logicId 逻辑编号
     * @param version 逻辑版本
     */
    void removeFromCache(String logicId, String version);


    /**
     * 将配置写入缓存
     *
     * @param logicId 逻辑编号
     * @return 逻辑配置
     */
    JSONObject saveToCache(String logicId, String version, JSONObject logicConfig);
}
