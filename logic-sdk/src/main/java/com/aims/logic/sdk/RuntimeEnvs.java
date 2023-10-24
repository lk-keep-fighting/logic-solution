package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.enums.EnvEnum;
import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuntimeEnvs {
    /**
     * 当前环境标识
     */
    private EnvEnum ENV;
    /**
     * 逻辑配置模式
     * online在线模式，通过IDE_HOST获取
     * offline离线模式，通过本地文件获取
     */
    private LogicConfigModelEnum LOGIC_CONFIG_MODEL;
    private String LOG;
    private String IDE_HOST;
    private String FORM_HOST;
    private String LOGIC_RUNTIME_HOST;
}

