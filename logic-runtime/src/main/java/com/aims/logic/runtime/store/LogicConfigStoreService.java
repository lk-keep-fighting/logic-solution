package com.aims.logic.runtime.store;

import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;

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
    JSONObject readLogicConfig(String logicId, String version);

    /**
     * 保存配置到本地
     *
     * @param logicId    逻辑编号
     * @param configJson 逻辑配置json字符串
     */
    String saveLogicConfigToFile(String logicId, String configJson);
}
