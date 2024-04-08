package com.aims.logic.runtime.contract.parser;

import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.contract.dsl.basic.StructurePropertyTreeNode;
import com.aims.logic.runtime.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.runtime.contract.enums.TypeKindEnum;
import com.aims.logic.runtime.util.DataType;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class TypeAnnotationParser {
    public static <T extends ParamTreeNode> JSONObject ParamsToJson(List<T> params) {
        JSONObject json = new JSONObject();
        if (params == null) {
            return null;
        }
        params.forEach(v -> {
            if (v.getDefaultValue() != null && !v.getDefaultValue().isBlank()) {
                System.out.println(v.getDefaultValue());
                var detValueTypeName = v.getTypeAnnotation().getTypeName();
                switch (detValueTypeName) {
                    case "object":
                        json.put(v.getName(), JSONObject.parse(v.getDefaultValue()));
                        break;
                    case "array":
                        json.put(v.getName(), JSONArray.parseArray(v.getDefaultValue()));
                        break;
                    case "boolean":
                        json.put(v.getName(), Boolean.parseBoolean(v.getDefaultValue()));
                        break;
                    case "number":
                        json.put(v.getName(), Float.parseFloat(v.getDefaultValue()));
                        break;
                    case "null":
                        json.put(v.getName(), null);
                        break;
                    case "function":
                    case "string":
                    case "date":
                    default:
                        json.put(v.getName(), v.getDefaultValue());
                        break;
                }
            } else {
                json.put(v.getName(), null);
            }
        });
        return json;
    }

    public static Class<?> ToClass(TypeAnnotationTreeNode typeAnno) throws ClassNotFoundException {
        String fullClassName = null;
        switch (typeAnno.getTypeName()) {
            case "number":
                fullClassName = Long.class.toString();
                break;
            case "string":
                fullClassName = String.class.toString();
                break;
            default:
                break;
        }
        return Class.forName(fullClassName);
    }

    public static TypeAnnotationTreeNode createTypeAnnotationTreeNode(Type paramType) {
        if (paramType instanceof ParameterizedType typeP) {
            List<TypeAnnotationTreeNode> typeArguments = Arrays.stream(typeP.getActualTypeArguments())
                    .map(TypeAnnotationParser::createTypeAnnotationTreeNode)
                    .collect(Collectors.toList());
            return new TypeAnnotationTreeNode()
                    .setTypeKind(TypeKindEnum.generic)
                    .setTypeName(typeP.getRawType().getTypeName())
                    .setTypeNamespace(paramType.getTypeName())
                    .setTypeArguments(typeArguments);
        } else if (paramType instanceof Class<?> clazz) {
            if (DataType.isSimpleDataType(clazz.getSimpleName()) || clazz.getTypeName().startsWith("java")) {
                return new TypeAnnotationTreeNode()
                        .setTypeKind(TypeKindEnum.primitive)
                        .setTypeName(paramType.getTypeName())
                        .setTypeNamespace(paramType.getTypeName())
                        .setDefaultValue(DataType.getNullValueString(clazz));
            } else if (clazz.isArray()) {
                Class<?> arrClazz = DataType.getJavaClass(clazz.getTypeName());
                return new TypeAnnotationTreeNode()
                        .setTypeKind(TypeKindEnum.primitiveArray)
                        .setTypeName(arrClazz.getName())
                        .setTypeNamespace(arrClazz.getName())
                        .setDefaultValue("[]");
            } else {
                List<StructurePropertyTreeNode> properties = Arrays.stream(clazz.getDeclaredFields())
                        .map(f -> new StructurePropertyTreeNode()
                                .setName(f.getName())
                                .setDefaultValue(DataType.getNullValueString(f.getGenericType()))
                                .setTypeAnnotation(createTypeAnnotationTreeNode(f.getGenericType())))
                        .collect(Collectors.toList());
                return new TypeAnnotationTreeNode()
                        .setTypeKind(TypeKindEnum.reference)
                        .setTypeName(paramType.getTypeName())
                        .setTypeNamespace(paramType.getTypeName())
                        .setProperties(properties);
            }
        }
        return null;
    }


//
//    private static final Map<Type, TypeAnnotationTreeNode> processedTypes = new HashMap<>();
//
//    public static TypeAnnotationTreeNode createTypeAnnotationTreeNode(Type paramType) {
//        TypeAnnotationTreeNode existingNode = processedTypes.keySet().stream()
//                .filter(type -> Objects.equals(type.getTypeName(), paramType.getTypeName()))
//                .map(processedTypes::get)
//                .findFirst()
//                .orElse(null);
//        if (existingNode != null) {
//            return existingNode; // 如果类型已处理过，则直接返回已创建的节点
//        }
//
//        TypeAnnotationTreeNode newNode = createTypeAnnotationTreeNodeInternal(paramType);
//        processedTypes.put(paramType, newNode); // 缓存已处理的类型和对应的节点
//        return newNode;
//    }
//
//    private static TypeAnnotationTreeNode createTypeAnnotationTreeNodeInternal(Type paramType) {
//        if (paramType instanceof Class<?>) {
//            Class<?> clazz = (Class<?>) paramType;
//            // 检查是否为基本数据类型或String
//            if (DataType.isSimpleDataType(clazz.getSimpleName()) || clazz.getSimpleName().equals("String")) {
//                return new TypeAnnotationTreeNode()
//                        .setTypeKind(TypeKindEnum.primitive)
//                        .setTypeName(paramType.getTypeName())
//                        .setTypeNamespace(paramType.getTypeName());
//            } else if (clazz.isArray()) {
//                Class<?> arrayClass = DataType.getJavaClass(clazz.getTypeName());
//                return new TypeAnnotationTreeNode()
//                        .setTypeKind(TypeKindEnum.primitiveArray)
//                        .setTypeName(arrayClass.getName())
//                        .setTypeNamespace(arrayClass.getName());
//            } else {
//                List<StructurePropertyTreeNode> properties = new ArrayList<>();
//                for (var field : clazz.getFields()) {
//                    Type fieldType = field.getGenericType();
//                    // 避免属性类型引用了类本身导致的死循环
//                    if (!fieldType.equals(clazz)) {
//                        TypeAnnotationTreeNode propertyNode = createTypeAnnotationTreeNode(fieldType);
//                        StructurePropertyTreeNode property = new StructurePropertyTreeNode()
//                                .setName(field.getName())
//                                .setTypeAnnotation(propertyNode);
//                        properties.add(property);
//                    }
//                }
//                return new TypeAnnotationTreeNode()
//                        .setTypeKind(TypeKindEnum.reference)
//                        .setTypeName(paramType.getTypeName())
//                        .setTypeNamespace(paramType.getTypeName())
//                        .setProperties(properties);
//            }
//        } else if (paramType instanceof ParameterizedType) {
//            ParameterizedType typeP = (ParameterizedType) paramType;
//            List<TypeAnnotationTreeNode> typeArguments = new ArrayList<>();
//            for (Type typeArg : typeP.getActualTypeArguments()) {
//                // 避免参数化类型的类型参数引用了类本身导致的死循环
//                if (!processedTypes.containsKey(typeArg)) {
//                    TypeAnnotationTreeNode typeArgNode = createTypeAnnotationTreeNode(typeArg);
//                    typeArguments.add(typeArgNode);
//                }
//            }
//            return new TypeAnnotationTreeNode()
//                    .setTypeKind(TypeKindEnum.generic)
//                    .setTypeName(typeP.getRawType().getTypeName())
//                    .setTypeNamespace(typeP.getTypeName())
//                    .setTypeArguments(typeArguments);
//        }
//
//        return null;
//    }
//

}