package com.aims.logic.service.classLoader;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class DynamicComponentLoader {

    private ConfigurableApplicationContext context;
    private final Map<String, URLClassLoader> loaders = new HashMap<>();

    public DynamicComponentLoader(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public void loadJar(String jarPath) throws Exception {
        File jarFile = new File(jarPath);
        URL jarUrl = jarFile.toURI().toURL();

        // 使用主应用的类加载器作为父加载器，确保 Spring 可见性
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                Thread.currentThread().getContextClassLoader()  // 关键点：父类加载器为 Spring 的类加载器
        );
        loaders.put(jarPath, classLoader);

        // 动态扫描并注册 Bean
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
                registry,
                false  // 不覆盖已有 Bean
        );
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.scan("com.aims.plugin.demo");  // JAR中组件所在的包
    }
}

