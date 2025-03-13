package com.aims.logic.runtime.util;

import com.aims.logic.runtime.contract.enums.KeepBizVersionEnum;
import com.aims.logic.runtime.env.LogicAppConfig;
import com.aims.logic.runtime.env.LogicSysEnvDto;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.alibaba.fastjson2.JSONObject;

import java.net.InetAddress;

public class RuntimeUtil {
    public static LogicConfigStoreService logicConfigStoreService;// = new LogicConfigStoreServiceImpl();
    private static JSONObject ENVs;
    private static LogicSysEnvDto ENVObject = null;
    public static LogicAppConfig AppConfig;


    /**
     * 获取强类型的环境变量，主要用于系统变量的方便读取
     *
     * @return 返回强类型环境变量
     */
    public static LogicSysEnvDto getEnvObject() {
        return ENVObject;
    }

    /**
     * 设置全局环境变量
     *
     * @param env
     */
    public static void setEnv(JSONObject env) {
        ENVs = env;
        if (ENVs != null)
            ENVObject = ENVs.toJavaObject(LogicSysEnvDto.class);

    }

    public static LogicSysEnvDto toEnvObject(JSONObject env) {
        if (env != null)
            return env.toJavaObject(LogicSysEnvDto.class);
        else
            return null;
    }

    /**
     * 获取原始配置的环境变量json，包含自定义的配置
     *
     * @return 原始配置的环境变量json
     */
    public static JSONObject getEnvJson() {
        if (ENVs == null) {
            initEnv();
        }
        return ENVs;
    }

    /**
     * 获取当前启动项目的url
     *
     * @return http://ip:port
     */
    public static String getUrl() {
        try {
            var host = InetAddress.getLocalHost();
            if (host == null) {
                throw new RuntimeException("获取当前程序Url失败");
            }

            String address = host.getHostAddress();
            String url = String.format("http://%s:%s", address, AppConfig.SERVER_PORT);

            System.out.println("读取本机Host:" + url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("获取当前程序Url失败: " + e.getMessage());
        }
    }

//    public static String getHeader(String headerName) {
//        if (RequestContextHolder.getRequestAttributes() == null)
//            return null;
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
//                .getRequestAttributes();
//        if (requestAttributes == null)
//            throw new RuntimeException("获取当前程序Request对象失败");
//        return requestAttributes.getRequest().getHeader(headerName);
//    }

    /**
     * 根据逻辑编号读取逻辑配置
     *
     * @param logicId 逻辑编号
     * @return 逻辑配置
     */
    public static JSONObject readLogicConfig(String logicId) {
        return logicConfigStoreService.readLogicConfig(logicId, null);
    }

    /**
     * 读取指定版本逻辑，若版本号不为null，则从表logic_bak读取
     *
     * @param logicId 逻辑编号
     * @param version 版本号
     * @return 指定版本的逻辑配置
     */
    public static JSONObject readLogicConfig(String logicId, String version) {
        if (RuntimeUtil.getEnvObject().getKEEP_BIZ_VERSION() == KeepBizVersionEnum.off)
            return logicConfigStoreService.readLogicConfig(logicId, null);
        else
            return logicConfigStoreService.readLogicConfig(logicId, version);
    }

    /**
     * 保存配置到本地
     *
     * @param logicId    逻辑编号
     * @param configJson 逻辑配置json字符串
     */
    public static String saveLogicConfigToFile(String logicId, String configJson) {
        return logicConfigStoreService.saveLogicConfigToFile(logicId, configJson);
    }

    /**
     * 读取配置文件中的环境变量
     *
     * @return 环境变量json对象
     */
    public static JSONObject readEnv() {
        JSONObject envIdx = FileUtil.readOrCreateFile(FileUtil.ENV_DIR, "index.json", "{\"env\":\"dev\"}");
        String _env = envIdx.get("env").toString();
        String envFileName = String.format("env.%s.json", _env);
        String defEnvFile = "{\"NODE_ENV\":\"" + _env
                + "\",\"LOGIC_CONFIG_MODEL\":\"online\",\"KEEP_BIZ_VERSION\":\"off\",\"IDE_HOST\":\"\",\"JWT\":{},\"LOG\":\"on\",\"DEFAULT_TRAN_SCOPE\":\"off\"}";
        return FileUtil.readOrCreateFile(FileUtil.ENV_DIR, envFileName, defEnvFile);
    }

    public static synchronized void initEnv() {
        if (ENVs == null)
            setEnv(readEnv());
    }
}
