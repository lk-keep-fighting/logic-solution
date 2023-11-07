package com.aims.logic.runtime.store;

import com.aims.logic.contract.enums.LogicConfigModelEnum;
import com.aims.logic.util.FileUtil;
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
public interface LogicConfigStoreService {

    /**
     * 根据逻辑编号读取逻辑配置
     *
     * @param logicId 逻辑编号
     * @return 逻辑配置
     */
    JSONObject readLogicConfig(String logicId);

    /**
     * 保存配置到本地
     *
     * @param logicId    逻辑编号
     * @param configJson 逻辑配置json字符串
     */
    String saveLogicConfigToFile(String logicId, String configJson);
}
