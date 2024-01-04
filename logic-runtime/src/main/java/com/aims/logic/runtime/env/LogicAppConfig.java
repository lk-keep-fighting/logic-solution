package com.aims.logic.runtime.env;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Configuration
public class LogicAppConfig {
    @Value("${logic.config-dir}")
    public String CONFIG_DIR;
}
