package com.aims.logic.runtime.env;

import com.aims.logic.runtime.contract.enums.EnvEnum;
import com.aims.logic.runtime.contract.enums.KeepBizVersionEnum;
import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogicAppEnvObject {
    /**
     * 当前环境标识
     */
    private String NODE_ENV;

//    /**
//     * 获取当前环境标识的枚举类型
//     */
//    public EnvEnum getNodeEnvEnum() {
//        return EnvEnum.valueOf(NODE_ENV);
//    }

    /**
     * 逻辑配置模式
     * online在线模式，通过IDE_HOST获取
     * offline离线模式，通过本地文件获取
     */
    private LogicConfigModelEnum LOGIC_CONFIG_MODEL;
    private KeepBizVersionEnum KEEP_BIZ_VERSION;
    private String LOG;
    private String IDE_HOST;
    private String FORM_HOST;
    private String LOGIC_RUNTIME_HOST;
    /**
     * 已发布的集成开发环境，具有相应的ide、runtime服务，可用于远程发布和调试
     */
    private List<PublishedIdeHost> PUBLISHED_IDE_HOSTS;
}

