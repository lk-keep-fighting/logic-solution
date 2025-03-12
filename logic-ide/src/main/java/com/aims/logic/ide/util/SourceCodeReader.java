package com.aims.logic.ide.util;

import com.aims.logic.ide.controller.dto.MethodSourceCodeDto;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;

public class SourceCodeReader {
    public static MethodSourceCodeDto readMethodSource(String sourceFilePath, String className, String methodName) throws Exception {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("源码文件不存在: " + sourceFilePath);
        }
//        JavaParser javaParser = new JavaParser();
//        // 解析源码
//        CompilationUnit cu = javaParser.parse(sourceFile).getResult().get();

        // 查找目标类
        ClassOrInterfaceDeclaration classDecl = StaticJavaParser.parse(sourceFile).getClassByName(className)
                .orElse(null);
        if (classDecl != null) {
            // 查找目标方法
            for (MethodDeclaration method : classDecl.getMethods()) {
                if (method.getNameAsString().equals(methodName)) {
                    return new MethodSourceCodeDto(sourceFilePath, method.toString(), method.getBegin().get().line, method.getEnd().get().line); // 返回方法的源码
                }
            }
            return null;
        } else return null;
    }
}