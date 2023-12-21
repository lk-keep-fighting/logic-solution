package com.aims.logic.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    /**
     * 合并JSON对象，用source覆盖target，返回覆盖后的JSON对象。
     *
     * @param source JSONObject
     * @param target JSONObject
     * @return JSONObject
     */
    public static JSONObject jsonMerge(JSONObject source, JSONObject target) {
        // 覆盖目标JSON为空，直接返回覆盖源
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }

        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (!target.containsKey(key)) {
                target.put(key, value);
            } else {
                if (value instanceof JSONObject valueJson) {
                    JSONObject targetValue = jsonMerge(valueJson, target.getJSONObject(key));
                    target.put(key, targetValue);
                } else if (value instanceof JSONArray valueArray) {
                    for (int i = 0; i < valueArray.size(); i++) {
                        JSONObject obj = (JSONObject) valueArray.get(i);
                        JSONObject targetValue = jsonMerge(obj, (JSONObject) target.getJSONArray(key).get(i));
                        target.getJSONArray(key).set(i, targetValue);
                    }
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

    public static Object toObject(ScriptObjectMirror mirror) {
        if (mirror.isEmpty()) {
            return null;
        }
        if (mirror.isArray()) {
            List<Object> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : mirror.entrySet()) {
                Object result = entry.getValue();
                if (result instanceof ScriptObjectMirror) {
                    list.add(toObject((ScriptObjectMirror) result));
                } else {
                    list.add(result);
                }
            }
            return list;
        }

        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : mirror.entrySet()) {
            Object result = entry.getValue();
            if (result instanceof ScriptObjectMirror) {
                map.put(entry.getKey(), toObject((ScriptObjectMirror) result));
            } else {
                map.put(entry.getKey(), result);
            }
        }
        return map;
    }
}
