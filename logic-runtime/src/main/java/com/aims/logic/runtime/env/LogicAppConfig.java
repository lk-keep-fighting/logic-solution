package com.aims.logic.runtime.env;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogicAppConfig {
    @Value("${logic.config-dir:./logic-configs}")
    public String CONFIG_DIR;

}
