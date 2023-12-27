package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.contract.dsl.ParamTreeNode;
import com.aims.logic.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.contract.dto.LogicItemRunResult;
import com.aims.logic.contract.enums.TypeKindEnum;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.util.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class JavaCodeFunction implements ILogicItemFunctionRunner {
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        var itemDsl = (LogicItemTreeNode) item;
        try {
            var clazz = ClassLoaderUtils.loadClass(itemDsl.getUrl().trim());
            System.out.printf("加载类：%s%n", itemDsl.getUrl().trim());
            var bodyObj = Functions.get("js").invoke(ctx, itemDsl.getBody()).getData();
            var methodName = itemDsl.getMethod().split("\\(")[0];

            List<Object> paramsArrayFromJsObj = new ArrayList<>();
            List<Class<?>> cls = new ArrayList<>();

            // 处理参数
            List<ParamTreeNode> paramTreeNodes = itemDsl.getParams();
            var paramsJson = JSONObject.from(JsonUtil.toObject((ScriptObjectMirror) bodyObj));

            for (ParamTreeNode param : paramTreeNodes) {
                var paramName = param.getName();
                var paramTypeAnno = param.getTypeAnnotation();
                var classWrapper = ClassWrapper.of(paramTypeAnno.getTypeNamespace());

                Class<?> paramClass = null;
                Object obj = null;

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
                        } else {
                            paramClass = ClassLoaderUtils.loadClass(classWrapper.getPackageName() + "." + classWrapper.getShortRawName());
                            var TypeParClazz = ClassLoaderUtils.loadClass(classWrapper.getParameterizedType().get(0).getName());
                            obj = JSONArray.parseArray(JSONObject.toJSONString(paramsJson.get(paramName)), TypeParClazz);
                            cls.add(paramClass);
                        }
                        break;

                    case primitiveArray:
                        paramClass = DataType.getJavaClass(paramTypeAnno.getTypeNamespace());
                        obj = paramsJson.getJSONArray(paramName).to(paramClass);
                        cls.add(paramClass);
                        break;

                    case primitive:
                        paramClass = DataType.getJavaClass(paramTypeAnno.getTypeNamespace());
                        obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(paramName)), paramClass);
                        cls.add(paramClass);
                        break;

                    default:
                        paramClass = ClassLoaderUtils.loadClass(classWrapper.getPackageName() + "." + classWrapper.getShortRawName());
                        cls.add(paramClass);
                        obj = JSONObject.parseObject(JSON.toJSONString(paramsJson.get(paramName)), paramClass);
                }

                if (paramClass == null) {
                    throw new RuntimeException(String.format("参数错误，当前方法未声明参数%s", paramName));
                }

                paramsArrayFromJsObj.add(obj);
            }

            Method method = clazz.getDeclaredMethod(methodName, cls.toArray(new Class<?>[]{}));
            LogicItemRunResult res = new LogicItemRunResult();
            try {
                var obj = method.invoke(SpringContextUtil.getBean(clazz), paramsArrayFromJsObj.toArray());
                res.setData(obj);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException().getCause());
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public String getItemType() {
        return "java";
    }

}
