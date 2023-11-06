package com.aims.logic.runtime.contract.parser;

import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public class TypeAnnotationParser {
    public static <T extends ParamTreeNode> JSONObject ParamsToJson(List<T> params) {
        JSONObject json = new JSONObject();
        if (params == null) {
            return null;
        }
        params.forEach(v -> {
            if (!v.getDefaultValue().isBlank()) {
                System.out.println(v.getDefaultValue());
                var detValueTypeName = v.getTypeAnnotation().getTypeName();
                switch (detValueTypeName) {
                    case "object":
                        json.put(v.getName(), JSONObject.parse(v.getDefaultValue()));
                        break;
                    case "array":
                        json.put(v.getName(), JSONArray.parseArray(v.getDefaultValue()));
                        System.out.println("转换array,parseArray");
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
}
