package com.aims.logic.ide.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LogicSdkMvcConfig implements WebMvcConfigurer {
//    private String staticPath = "/Users/lk/Documents/Dev/aims/xuanwu-logic/logic-solution/logic-sdk/web";
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**/*.*").addResourceLocations("file:" + staticPath);
//    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/logic").setViewName("/index.html");
    }
}