package com.aims.logic.sdk.config;

import com.aims.logic.sdk.service.LogicLogService;
import com.aims.logic.sdk.service.impl.LogicLogServiceImpl;
import com.aims.logic.sdk.service.impl.es.LogicLogServiceEsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogicLogServiceConfig {
    @Value("${logic.log.store:db}")
    public String logStoreType;
    @Bean
    public LogicLogService logicLogService() {
        if ("es".equals(logStoreType)) {
            return new LogicLogServiceEsImpl();
        } else {
            return new LogicLogServiceImpl();
        }
    }
}