package com.aims.logic.runtime.env;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LogicAppConfig {
    @Value("${logic.config-dir:./logic-configs}")
    public String CONFIG_DIR;

    @Value("${logic.biz-error-classes:LogicBizException}")
    public List<String> BIZ_ERROR_CLASSES;

    @Value("${server.port}")
    public String SERVER_PORT;

}
