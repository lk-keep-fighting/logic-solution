package com.aims.logic.runtime.env;

import com.aims.logic.runtime.contract.enums.KeepBizVersionEnum;
import com.aims.logic.runtime.contract.enums.LogicConfigModelEnum;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
public class LogicSysEnvDto {
    /**
     * 当前环境标识
     */
    private String NODE_ENV;

    /**
     * 逻辑配置模式
     * online在线模式，通过IDE_HOST获取
     * offline离线模式，通过本地文件获取
     */
    private LogicConfigModelEnum LOGIC_CONFIG_MODEL;
    private KeepBizVersionEnum KEEP_BIZ_VERSION;
    private String LOG;
    /**
     * 默认事务模式
     */
    private LogicItemTransactionScope DEFAULT_TRAN_SCOPE;
    private String IDE_HOST;
    private String FORM_HOST;
    private String LOGIC_RUNTIME_HOST;
    /**
     * 已发布的集成开发环境，具有相应的ide、runtime服务，可用于远程发布和调试
     */
    private List<LogicEnvPublishedIdeHost> PUBLISHED_IDE_HOSTS;

    public LogicItemTransactionScope getDefaultTranScope() {
        if (DEFAULT_TRAN_SCOPE == null || StringUtils.isEmpty(DEFAULT_TRAN_SCOPE.getValue()))
            return LogicItemTransactionScope.everyJavaNode;
        else
            return DEFAULT_TRAN_SCOPE;
    }
}

