/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aims.logic.ide.util;

import com.aims.logic.ide.controller.dto.MethodDto;
import com.aims.logic.ide.controller.dto.MethodSourceCodeDto;
import com.aims.logic.ide.controller.dto.LogicClassDto;
import com.aims.logic.runtime.util.ClassLoaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yusu
 */
@Slf4j
public class ClassUtils {

    public static <T> T newInstance(String className) throws Exception {
        return (T) newInstance(ClassLoaderUtils.loadClass(className));
    }

    public static <T> T newInstance(String className, Class referrer) throws Exception {
        return newInstance(ClassLoaderUtils.loadClass(className, referrer));
    }

    public static <T> T newInstance(String className, ClassLoader classLoader) throws Exception {
        return newInstance(ClassLoaderUtils.loadClass(className, classLoader));
    }

    public static <T> T newInstance(Class clazz) throws Exception {
        Constructor constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
    }

    public static boolean isAbstractOrInterface(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers());
    }

    public static Method getDeclaredMethod(String fullClassPath, String methodName, List<String> paramFullClassPaths) throws ClassNotFoundException, NoSuchMethodException {
        var clazz = ClassLoaderUtils.loadClass(fullClassPath);
        List<Class<?>> parameterTypes = new ArrayList<>();
        paramFullClassPaths.forEach((v) -> {
            try {
                parameterTypes.add(Class.forName(v));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        var cls = parameterTypes.toArray(new Class<?>[]{});
        return clazz.getDeclaredMethod(methodName, cls);
    }

    public static Method getDeclaredMethod(String fullClassPath, String methodName, Class<?>[] paramTypes) throws ClassNotFoundException, NoSuchMethodException {
        var clazz = ClassLoaderUtils.loadClass(fullClassPath);

        return clazz.getDeclaredMethod(methodName, paramTypes);
    }

    public static List<Method> getDeclaredMethods(String fullClassPath) throws ClassNotFoundException {
        var clazz = ClassLoaderUtils.loadClass(fullClassPath);
        return getDeclaredMethods(clazz);
    }

    public static List<Method> getDeclaredMethods(Class<?> clazz) {
        var methods = clazz.getDeclaredMethods();
        return Arrays.stream(methods).toList();
    }

    public static List<Method> getMethods(String fullClassPath) throws ClassNotFoundException {
        var clazz = ClassLoaderUtils.loadClass(fullClassPath);
        return getMethods(clazz);
    }

    public static List<MethodDto> getMethodsAndSourceCode(String fullClassPath) throws ClassNotFoundException {
        var clazz = ClassLoaderUtils.loadClass(fullClassPath);
        var methods = getMethods(clazz);
        List<MethodDto> methodDtos = new ArrayList<>();
        // 推导源码路径
        String sourceFilePath = ClassLoaderUtils.getResource(clazz.getName().replace('.', '/') + ".class")
                .getPath()
                .replace("target/classes", "src/main/java")
                .replace(".class", ".java");
        for (int i = 0; i < methods.size(); i++) {
            // 读取方法的源码
            MethodSourceCodeDto methodSource = null;
            try {
                methodSource = SourceCodeReader.readMethodSource(
                        sourceFilePath, clazz.getSimpleName(), methods.get(i).getName()
                );
            } catch (Exception e) {
                log.warn("读取{}源码失败: {}", methods.get(i).getName(), e.getMessage());
            }
            methodDtos.add(new MethodDto(methods.get(i), methodSource));
        }

        return methodDtos;
    }

    public static List<Method> getMethods(Class<?> clazz) {
        var methods = clazz.getMethods();
        return Arrays.stream(methods).toList();
    }

    public static List<Method> getMethodsByAnnotation(String fullClassPath, Class<?> annotationClass) throws ClassNotFoundException {
        var methods = getMethods(fullClassPath);
        return methods.stream().filter(method -> method.isAnnotationPresent((Class<? extends Annotation>) annotationClass)).toList();
    }

    public static List<MethodDto> getMethodsAndSourceCodeByAnnotation(String fullClassPath, Class<?> annotationClass) throws Exception {
        var methodDtos = getMethodsAndSourceCode(fullClassPath);
        return methodDtos.stream().filter(dto -> dto.getMethod().isAnnotationPresent((Class<? extends Annotation>) annotationClass)).toList();
    }

    public static List<LogicClassDto> getAllClassNames(String basePackage) {
        List<LogicClassDto> classNames = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(resourcePatternResolver);

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class)); // 替换成你想要的类型

        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                basePackage.replace('.', '/') + "/**/*.class";

        org.springframework.core.io.Resource[] resources = new org.springframework.core.io.Resource[0];
        try {
            resources = resourcePatternResolver.getResources(packageSearchPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (org.springframework.core.io.Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = null;
                try {
                    metadataReader = metadataReaderFactory.getMetadataReader(resource);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String className = metadataReader.getClassMetadata().getClassName();
                classNames.add(new LogicClassDto(className));
            }
        }

        // 打印类路径
        for (var classPath : classNames) {
            log.debug("className: " + classPath.getValue());
        }
        return classNames;
    }

}
