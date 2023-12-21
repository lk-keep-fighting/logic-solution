package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.contract.dsl.ParamTreeNode;
import com.aims.logic.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.contract.dto.LogicItemRunResult;
import com.aims.logic.contract.enums.TypeKindEnum;
import com.aims.logic.contract.parser.TypeAnnotationParser;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.JavaCodeFunctionService;
import com.aims.logic.util.*;
import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.nashorn.api.scripting.JSObject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class JavaCodeFunction implements JavaCodeFunctionService {
    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        var itemDsl = ((LogicItemTreeNode) item);
//            ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        try {
            var clazz = ClassLoaderUtils.loadClass(itemDsl.getUrl().trim());
            System.out.printf("load class:%s%n", itemDsl.getUrl().trim());
            var bodyObj = Functions.get("js").invoke(ctx, itemDsl.getBody()).getData();
            var methodName = itemDsl.getMethod();
            try {
                //参数声明
                List<ParamTreeNode> paramTreeNodes = itemDsl.getParams();
                //提交参数，实参
//                JSONObject paramsJson = JSONObject.from(bodyObj);
                var paramsMap = JsonUtil.toObject((ScriptObjectMirror) bodyObj);
                var paramsJson = JSONObject.from(paramsMap);
                //将参数转换为数组，用于反射调用
                List<Object> paramsArrayFromJsObj = new ArrayList<>();
                //将参数声明转换为参数名-参数类型类的键值对，用于反射调用
                Map<String, TypeAnnotationTreeNode> parameterTypes = new HashMap<>();
                paramTreeNodes.forEach(p -> {
                    parameterTypes.put(p.getName(), p.getTypeAnnotation());
                });
                ScriptObjectMirror paramJsObject = (ScriptObjectMirror) bodyObj;

                List<Class<?>> cls = new ArrayList<>();
                paramJsObject.forEach((k, v) -> {
                    var paramTypeAnno = parameterTypes.get(k);
                    Class<?> paramClass = null;
                    ClassWrapper classWrapper = ClassWrapper.of(paramTypeAnno.getTypeNamespace());
                    try {
//                        var objectValue = JSONObject.parseObject(JSON.toJSONString(v), paramClass);
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        ScriptObjectMirror objV = (ScriptObjectMirror) v;
//                        var vJson = JSON.toJSONString(v);
//                        var objVjson = objV.to(JSONObject.class);
                        Object obj;
                        if (paramTypeAnno.getTypeKind() == TypeKindEnum.generic) {
                            if (Objects.equals(paramTypeAnno.getTypeName(), Map.class.getTypeName())) {
                                throw new RuntimeException("暂不支持Map类型，请使用其他对象，如JSONObject代替");
//                                var parTypes = classWrapper.getParameterizedType();
//                                var key = parTypes.get(0);
//                                var value = parTypes.get(1);
//                                var keyClazz = ClassLoaderUtils.loadClass(key.getName());
//                                var keyIns = keyClazz.getDeclaredConstructor().newInstance();
//                                var valClazz = ClassLoaderUtils.loadClass(value.getName());
//                                var valIns = valClazz.getDeclaredConstructor().newInstance();
//                                Type mapType = new ParameterizedType() {
//                                    @Override
//                                    public Type[] getActualTypeArguments() {
//                                        return new Type[]{keyClazz, valClazz};
//                                    }
//
//                                    @Override
//                                    public Type getRawType() {
//                                        return Map.class;
//                                    }
//
//                                    @Override
//                                    public Type getOwnerType() {
//                                        return null;
//                                    }
//                                };
//                                var mapIns = mapType.getClass().getDeclaredConstructor().newInstance();
//                                var targetMap = Map.copyOf((Map) mapIns);
//                                paramClass = targetMap.getClass();
//                                obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(k)), paramClass);
//                                cls.add(paramClass);
                            } else {
                                paramClass = ClassLoaderUtils.loadClass(classWrapper.getPackageName() + "." + classWrapper.getShortRawName());
                                var TypeParClazz = ClassLoaderUtils.loadClass(classWrapper.getParameterizedType().get(0).getName());
                                obj = JSONArray.parseArray(JSONObject.toJSONString(paramsJson.get(k)), TypeParClazz);
                                cls.add(paramClass);
                            }
                        } else if (paramTypeAnno.getTypeKind() == TypeKindEnum.primitiveArray) {
                            paramClass = DataType.getJavaClass(paramTypeAnno.getTypeNamespace());
                            obj = paramsJson.getJSONArray(k).to(paramClass);
                            cls.add(paramClass);
                        } else if (paramTypeAnno.getTypeKind() == TypeKindEnum.primitive) {
                            paramClass = DataType.getJavaClass(paramTypeAnno.getTypeNamespace());
                            obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(k)), paramClass);
                            cls.add(paramClass);
                        } else {
                            paramClass = ClassLoaderUtils.loadClass(classWrapper.getPackageName() + "." + classWrapper.getShortRawName());
                            cls.add(paramClass);
                            obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(k)), paramClass);
                        }
                        paramsArrayFromJsObj.add(obj);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (paramClass == null) {
                        throw new RuntimeException(String.format("参数错误，参数%s在当前方法未声明", k));
                    }
//                    try {
//                        if (v == null | paramClass.isPrimitive() | paramTypeAnno.getTypeKind() == TypeKindEnum.primitive | paramClass.getName().equals("java.lang.Integer") | paramClass.getName().equals("java.lang.String") | paramClass.getName().equals("java.lang.Object")) {
//                            paramsArrayFromJsObj.add(v);
//                        } else {
//                            ScriptObjectMirror objV = (ScriptObjectMirror) v;
//                            Object objectValue;
//                            if (objV.isArray() | paramTypeAnno.getTypeKind() == TypeKindEnum.generic) {
//                                var TypeParClazz = ClassLoaderUtils.loadClass(classWrapper.getParameterizedType().get(0).getName());
//                                Object[] values = objV.values().toArray();
//                                objectValue = JSONArray.from(values).toJavaList(TypeParClazz);
//                            } else {
//                                var parClazz = paramClass.getDeclaredConstructor().newInstance();
//                                for (var field : parClazz.getClass().getDeclaredFields()) {
//                                    field.setAccessible(true);
//                                    if (objV.containsKey(field.getName())) {
//                                        var value = objV.get(field.getName());
//                                        if (value instanceof ScriptObjectMirror) {
//                                            JSObject jsObject = (ScriptObjectMirror) value;
//                                            if (jsObject.isArray()) {
//                                                Object[] values = jsObject.values().toArray();
//                                                var fieldProperty = paramTypeAnno.getProperties().stream().filter(a -> a.getName().equals(field.getName())).findFirst().orElse(null);
//                                                if (fieldProperty != null) {
//                                                    ClassWrapper fieldClassWrapper = ClassWrapper.of(fieldProperty.getTypeAnnotation().getTypeNamespace());
//                                                    var TypeParClazz = ClassLoaderUtils.loadClass(fieldClassWrapper.getParameterizedType().get(0).getName());
//                                                    objectValue = JSONArray.from(values).toJavaList(TypeParClazz);
//                                                    field.set(parClazz, objectValue);
//                                                } else {
//                                                    throw new RuntimeException(String.format("未发现属性%s的声明！", field.getName()));
//                                                }
//                                            } else {
//                                                field.set(parClazz, value);
////                                                System.err.printf("javaCodeRunner类型未匹配，未处理的字段:%s", field.getName());
//                                            }
//                                        } else {
//                                            field.set(parClazz, value);
//                                        }
//                                    }
//                                }
//                                objectValue = parClazz;
//                            }
//                            paramsArrayFromJsObj.add(objectValue);
//                        }
//                    } catch (InvocationTargetException ex) {
//                        throw new RuntimeException(ex);
//                    } catch (InstantiationException ex) {
//                        throw new RuntimeException(ex);
//                    } catch (IllegalAccessException ex) {
//                        throw new RuntimeException(ex);
//                    } catch (NoSuchMethodException ex) {
//                        throw new RuntimeException(ex);
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
                });
                Method method = clazz.getDeclaredMethod(methodName, cls.toArray(new Class<?>[]{}));
                LogicItemRunResult res = new LogicItemRunResult();
                var obj = method.invoke(clazz.getDeclaredConstructor().newInstance(), paramsArrayFromJsObj.toArray());
                res.setData(obj);
                return res;
            } catch (
                    NoSuchMethodException e) {
                ctx.setHasErr(true);
                ctx.setErrMsg(String.format("未找到方法%s,请检查方法名与提交参数是否与实际一致！", methodName));
                System.err.println(e.toString());
                return new LogicItemRunResult().setData(e);
            }
        } catch (
                Exception e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.toString());
            System.err.println(e.toString());
            return new LogicItemRunResult().setData(e);
        }

    }

    @Override
    public String getItemType() {
        return "java";
    }

}
