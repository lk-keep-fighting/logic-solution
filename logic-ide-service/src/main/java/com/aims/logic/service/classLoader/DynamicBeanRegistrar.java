package com.aims.logic.service.classLoader;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;

public class DynamicBeanRegistrar {

    public static void registerBean(ApplicationContext context, Class<?> clazz) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
        beanFactory.registerBeanDefinition(clazz.getName(), beanDefinition);
        System.out.println("注册 Bean: " + clazz.getName());
    }
}