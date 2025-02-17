package com.aims.logic.service;

import com.aims.logic.runtime.util.SpringContextUtil;
import com.aims.logic.service.classLoader.DynamicBeanRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

@SpringBootApplication(scanBasePackages = {"com.aims.logic", "com.aims.datamodel"})
@SpringBootConfiguration
public class LogicIdeServiceApplication {
    private static final String DYNAMIC_LIBS_PATH = "/Users/lk/Documents/Dev/aims/xuanwu-logic/logic-solution/logic-ide-service/dynamic-libs"; // 自定义文件夹路径

    public static void main(String[] args) throws Exception {
        var context = SpringApplication.run(LogicIdeServiceApplication.class);
//        DynamicComponentLoader dynamicComponentLoader = new DynamicComponentLoader(context);
//        dynamicComponentLoader.loadJar(DYNAMIC_LIBS_PATH + "/test-case-0.0.1-SNAPSHOT.jar");
    }

    private void solution1() throws IOException {
        // 1. 扫描 dynamic-libs 文件夹中的 JAR 包
        File libFolder = new File(DYNAMIC_LIBS_PATH);
        if (!libFolder.exists() || !libFolder.isDirectory()) {
            throw new RuntimeException("动态库文件夹不存在: " + DYNAMIC_LIBS_PATH);
        }

        File[] jarFiles = libFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            throw new RuntimeException("动态库文件夹中没有 JAR 包: " + DYNAMIC_LIBS_PATH);
        }

        // 2. 加载 JAR 包
        URLClassLoader classLoader = null;
        try {
            classLoader = new URLClassLoader(
                    java.util.Arrays.stream(jarFiles)
                            .map(file -> {
                                try {
                                    return file.toURI().toURL();
                                } catch (IOException e) {
                                    throw new RuntimeException("无法加载 JAR 文件: " + file.getAbsolutePath(), e);
                                }
                            })
                            .toArray(URL[]::new)
            );
            var context = SpringApplication.run(LogicIdeServiceApplication.class);

            // 3. 加载 JAR 包中的类
            for (File jarFile : jarFiles) {
                try (JarFile jar = new JarFile(jarFile)) {
                    URLClassLoader finalClassLoader = classLoader;
                    jar.entries().asIterator().forEachRemaining(entry -> {
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().replace('/', '.').replace(".class", "");
                            try {
                                Class<?> clazz = finalClassLoader.loadClass(className);
                                DynamicBeanRegistrar.registerBean(SpringContextUtil.getApplicationContext(), clazz);
                                System.out.println("加载类: " + clazz.getName());
                                // 在这里可以将类注册到 Spring 容器
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException("无法加载类: " + className, e);
                            }
                        }
                    });
                }
            }

            // 4. 启动 Spring Boot 应用
//            ApplicationContext context = new AnnotationConfigApplicationContext();

        } finally {
            if (classLoader != null) {
                classLoader.close();
            }
        }
    }

}
