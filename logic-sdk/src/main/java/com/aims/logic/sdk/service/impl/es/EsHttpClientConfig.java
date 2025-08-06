package com.aims.logic.sdk.service.impl.es;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class EsHttpClientConfig {

    @Value("${logic.log.es.username:}")
    public String esUsername;

    @Value("${logic.log.es.password:}")
    public String esPassword;

    @Bean
    public OkHttpClient esHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (StringUtils.hasText(esUsername) && StringUtils.hasText(esPassword)) {
            builder.addInterceptor(new BasicAuthInterceptor(esUsername, esPassword));
        }
        return builder.build();
    }
}
