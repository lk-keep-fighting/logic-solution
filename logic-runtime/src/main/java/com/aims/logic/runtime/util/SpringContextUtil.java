package com.aims.logic.runtime.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public final class SpringContextUtil implements ApplicationContextAware {

    // 获取 ApplicationContext
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        // 先判断是否存在上下文对象
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        // 先判断是否存在上下文对象
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(beanName, clazz);
    }

    /**
     * 获取所有的 Bean
     *
     * @return Bean名称与类型
     */
    public static Map<String, Class<?>> getAllBeansAsMap() {
        Map<String, Class<?>> beanMap = new HashMap<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Class<?> beanClass = applicationContext.getType(beanName);
            if (beanClass != null) {
                beanMap.put(beanName, beanClass);
            }
        }
        return beanMap;
    }
}