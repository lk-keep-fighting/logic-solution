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

import com.aims.logic.ide.controller.dto.LogicClassDto;
import com.aims.logic.ide.controller.dto.MethodSourceCodeDto;
import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.util.ClassLoaderUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    public static MethodSourceCodeDto getMethodSourceCode(String fullClassPath, String methodName,
                                                          List<ParamTreeNode> paramTreeNodeList) throws ClassNotFoundException {
        var clazz = ClassLoaderUtils.loadClass(fullClassPath);

        // 方案1：尝试从标准源码路径读取（适用于开发环境）
        MethodSourceCodeDto result = tryReadFromSourcePath(clazz, methodName, paramTreeNodeList);
        if (result != null) {
            return result;
        }
//
//        // 方案2：尝试从类路径读取（适用于JAR包环境）
//        result = tryReadFromClasspath(clazz, methodName, paramTreeNodeList);
//        if (result != null) {
//            return result;
//        }
//
//        // 方案3：尝试从源码JAR读取（如果有源码JAR）
//        result = tryReadFromSourceJar(clazz, methodName, paramTreeNodeList);
//        if (result != null) {
//            return result;
//        }

        log.warn("无法读取方法源码: {}.{}", fullClassPath, methodName);
        return null;
    }

    public static List<Method> getMethods(Class<?> clazz) {
        var methods = clazz.getMethods();
        return Arrays.stream(methods).toList();
    }

    private static MethodSourceCodeDto tryReadFromSourcePath(Class<?> clazz, String methodName,
                                                             List<ParamTreeNode> paramTreeNodeList) {
        try {
            // 开发环境下的源码路径推导
            String sourcePath = clazz.getResource(clazz.getSimpleName() + ".class").getPath()
                    .replace("target/classes", "src/main/java")
                    .replace(".class", ".java");
            log.info("尝试调试环境读取源码: {}", sourcePath);
            File sourceFile = new File(sourcePath);
            if (sourceFile.exists()) {
                return parseMethodFromSource(new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8),
                        clazz.getSimpleName(), methodName, paramTreeNodeList);
            }
        } catch (Exception e) {
            log.debug("尝试从源码路径读取失败: {}", e.getMessage());
        }
        return null;
    }

//    private static MethodSourceCodeDto tryReadFromClasspath(Class<?> clazz, String methodName,
//                                                            List<ParamTreeNode> paramTreeNodeList) {
//        try {
//            // 类路径下的源码文件路径
//            String sourcePath = clazz.getName().replace('.', '/') + ".java";
//            log.info("尝试从类路径读取源码: {}", sourcePath);
//
//            try (InputStream is = clazz.getClassLoader().getResourceAsStream(sourcePath)) {
//                if (is != null) {
//                    String sourceCode = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//                    return parseMethodFromSource(sourceCode, clazz.getSimpleName(),
//                            methodName, paramTreeNodeList);
//                }
//            }
//        } catch (Exception e) {
//            log.debug("尝试从类路径读取失败: {}", e.getMessage());
//        }
//        return null;
//    }
//
//    private static MethodSourceCodeDto tryReadFromSourceJar(Class<?> clazz, String methodName,
//                                                            List<ParamTreeNode> paramTreeNodeList) {
//        try {
//            // 尝试从-sources.jar中读取
//            String sourceJarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath()
//                    .replace(".jar", "-sources.jar");
//
//            try (JarFile jarFile = new JarFile(sourceJarPath)) {
//                String entryPath = clazz.getName().replace('.', '/') + ".class";
//                JarEntry entry = jarFile.getJarEntry(entryPath);
//
//                if (entry != null) {
//                    try (InputStream is = jarFile.getInputStream(entry)) {
//                        String sourceCode = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//                        return parseMethodFromSource(sourceCode, clazz.getSimpleName(),
//                                methodName, paramTreeNodeList);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.debug("尝试从源码JAR读取失败: {}", e.getMessage());
//        }
//        return null;
//    }

    private static MethodSourceCodeDto parseMethodFromSource(String sourceCode, String className,
                                                             String methodName, List<ParamTreeNode> paramTreeNodeList) {
        // 使用JavaParser解析源码
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);

        if (!parseResult.isSuccessful() || !parseResult.getResult().isPresent()) {
            return null;
        }

        CompilationUnit cu = parseResult.getResult().get();
        MethodDeclaration[] foundMethod = new MethodDeclaration[1];

        // 查找匹配的方法
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration md, Void arg) {
                super.visit(md, arg);
                if (md.getNameAsString().equals(methodName)) {
                    if (paramTreeNodeList == null || paramTreeNodeList.isEmpty() ||
                            md.getParameters().size() == paramTreeNodeList.size()) {
                        foundMethod[0] = md;
                    }
                }
            }
        }.visit(cu, null);

        if (foundMethod[0] == null) {
            return null;
        }

        MethodSourceCodeDto dto = new MethodSourceCodeDto();
        dto.setSourceCode(foundMethod[0].toString());
        dto.setBeginLine(foundMethod[0].getRange().map(r -> r.begin.line).orElse(-1));
        dto.setEndLine(foundMethod[0].getRange().map(r -> r.end.line).orElse(-1));
        return dto;
    }

    public static List<Method> getMethodsByAnnotation(String fullClassPath, Class<?> annotationClass) throws ClassNotFoundException {
        var methods = getMethods(fullClassPath);
        return methods.stream().filter(method -> method.isAnnotationPresent((Class<? extends Annotation>) annotationClass)).toList();
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