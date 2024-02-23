package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.runtime.util.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JavaCodeFunction implements ILogicItemFunctionRunner {
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        log.info("-------开始执行Java代码-------");
        var itemDsl = (LogicItemTreeNode) item;
        try {
            var clazz = ClassLoaderUtils.loadClass(itemDsl.getUrl().trim());
            log.info("成功加载方法所在类：{}", itemDsl.getUrl().trim());
            var bodyObj = Functions.get("js").invoke(ctx, itemDsl.getBody()).getData();//执行js脚本，返回方法实参
            log.info("Java代码实参类型：-{}", bodyObj.getClass());
            var methodName = itemDsl.getMethod().split("\\(")[0];
            // 获取参数声明
            List<ParamTreeNode> paramTreeNodes = itemDsl.getParams();
            var paramsJson = bodyObj instanceof ScriptObjectMirror ? JSONObject.from(JsonUtil.toObject((ScriptObjectMirror) bodyObj)) : JSONObject.from(bodyObj);
            log.info("Java代码实参：-{}", paramsJson.toJSONString());
            itemDsl.setBody(paramsJson.toJSONString());
            List<Class<?>> cls = new ArrayList<>();
            List<Object> paramsArrayFromJsObj = new ArrayList<>();
            for (int i = 0; i < paramTreeNodes.size(); i++) {
                ParamTreeNode param = paramTreeNodes.get(i);
                var paramName = param.getName();
                var paramTypeAnno = param.getTypeAnnotation();
                var classWrapper = ClassWrapper.of(paramTypeAnno.getTypeNamespace());
                Class<?> paramClass = null;
                Object inputParamValue = paramsJson.get(paramName);//参数可能为代码中ByMap传入，有强类型声明
                if (inputParamValue == null) {//可能通过ByObjectArgs动态参数传入，自动生成的参数名_p1、_p2、_p3...
                    inputParamValue = paramsJson.get("_p" + (i + 1));
                }
                Object obj = inputParamValue;
                if (inputParamValue != null) {
                    //获取传入的数据的类型声明，用于判断与方法声明是否一致，如果一直，则不用转换
                    Class<?> inputParamClass = inputParamValue.getClass();
                    if (Objects.equals(inputParamClass.getTypeName(), paramTypeAnno.getTypeNamespace())) {
                        paramClass = inputParamClass;
                    } else {
                        paramsJson.put(paramName, inputParamValue);
                    }
                }
                if (paramClass == null) {
                    switch (paramTypeAnno.getTypeKind()) {
                        case generic:
                            if (paramTypeAnno.getTypeName().equals(Map.class.getTypeName())) {
                                var parTypes = classWrapper.getParameterizedType();
                                var key = parTypes.get(0);
                                var value = parTypes.get(1);
                                var keyClazz = ClassLoaderUtils.loadClass(key.getName());
                                var valClazz = ClassLoaderUtils.loadClass(value.getName());
                                obj = paramsJson.getJSONObject(paramName).to(TypeReference.mapType(Map.class, keyClazz, valClazz));
                                paramClass = obj.getClass();
                                cls.add(paramClass);
                            } else {//List
                                paramClass = ClassLoaderUtils.loadClass(classWrapper.getPackageName() + "." + classWrapper.getShortRawName());
                                var TypeParClazz = ClassLoaderUtils.loadClass(classWrapper.getParameterizedType().get(0).getName());
                                obj = JSONArray.parseArray(JSONObject.toJSONString(paramsJson.get(paramName)), TypeParClazz);
                            }
                            break;
                        case primitiveArray:
                            paramClass = DataType.getJavaClass(paramTypeAnno.getTypeNamespace());
                            obj = paramsJson.getJSONArray(paramName).to(paramClass);
                            break;
                        case primitive:
                            paramClass = DataType.getJavaClass(paramTypeAnno.getTypeNamespace());
                            obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(paramName)), paramClass);
                            break;
                        default:
                            paramClass = ClassLoaderUtils.loadClass(classWrapper.getPackageName() + "." + classWrapper.getShortRawName());
                            obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(paramName)), paramClass);
                    }
                }
                if (paramClass == null) {
                    throw new RuntimeException(String.format("参数错误，当前方法未声明参数%s", paramName));
                }
                cls.add(paramClass);
                paramsArrayFromJsObj.add(obj);
            }

            Method method = findMethod(clazz, methodName, cls.toArray((new Class<?>[]{})));
            LogicItemRunResult res = new LogicItemRunResult();
            try {
                var obj = method.invoke(SpringContextUtil.getBean(clazz), paramsArrayFromJsObj.toArray());
                res.setData(obj);
            } catch (InvocationTargetException e) {//抛出异常触发事务回滚
                log.info(">>>logic执行方法{}发生错误", methodName);
                log.info(e.getTargetException().getMessage());
                e.printStackTrace();
                return res.setSuccess(false)
                        .setMsg(e.getTargetException().getMessage());
            }
            return res;
        } catch (Exception e) {
            log.error(">>>java节点意外的异常");
            e.printStackTrace();
            var msg = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
            return new LogicItemRunResult().setSuccess(false)
                    .setMsg(msg);
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(methodName, parameterTypes);
    }

    @Override
    public String getItemType() {
        return "java";
    }

}
