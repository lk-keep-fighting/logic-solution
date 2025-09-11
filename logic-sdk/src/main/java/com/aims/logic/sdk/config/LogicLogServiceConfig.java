package com.aims.logic.sdk.config;

import com.aims.logic.sdk.service.LogicLogService;
import com.aims.logic.sdk.service.impl.LogicLogServiceImpl;
import com.aims.logic.sdk.service.impl.es.LogicLogServiceEsImpl;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class LogicLogServiceConfig {
    @Value("${logic.log.store:db}")
    public String logStoreType;
    public OkHttpClient esHttpClient;

    @Bean
    public LogicLogService logicLogService() {
        if ("es".equals(logStoreType)) {
            esHttpClient = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .callTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();
            return new LogicLogServiceEsImpl(esHttpClient);
        } else {
            return new LogicLogServiceImpl();
        }
    }
}