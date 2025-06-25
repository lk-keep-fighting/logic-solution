//package com.aims.logic.ide.util;
//
//import com.aims.logic.ide.controller.dto.MethodSourceCodeDto;
//import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.List;
//
//public class SourceCodeReader {
//    public static MethodSourceCodeDto readMethodSource(String sourceFilePath, String className, String methodName, List<ParamTreeNode> paramTreeNodeList) throws Exception {
//        File sourceFile = new File(sourceFilePath);
//        if (!sourceFile.exists()) {
//            throw new FileNotFoundException("源码文件不存在: " + sourceFilePath);
//        }
////        JavaParser javaParser = new JavaParser();
////        // 解析源码
////        CompilationUnit cu = javaParser.parse(sourceFile).getResult().get();
//
//        // 查找目标类
//        ClassOrInterfaceDeclaration classDecl = StaticJavaParser.parse(sourceFile).getClassByName(className)
//                .orElse(null);
//        if (classDecl != null) {
//            // 查找目标方法
//            for (MethodDeclaration method : classDecl.getMethods()) {
//
//                if (method.getNameAsString().equals(methodName)) {
//                    if (paramTreeNodeList.size() != method.getParameters().size()) {
//                        continue;
//                    } else {
//                        var isAllMatch = true;
//                        for (int i = 0; i < paramTreeNodeList.size(); i++) {
//                            if (!method.getParameters().get(i).getNameAsString().equals(paramTreeNodeList.get(i).getName())) {
//                                isAllMatch = false;
//                                break;
//                            }
//                        }
//                        if (isAllMatch)
//                            return new MethodSourceCodeDto(sourceFilePath, method.toString(), method.getBegin().get().line, method.getEnd().get().line); // 返回方法的源码
//                    }
//                }
//            }
//            return null;
//        } else return null;
//    }
//}