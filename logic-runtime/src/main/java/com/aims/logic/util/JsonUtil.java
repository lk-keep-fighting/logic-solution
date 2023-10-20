package com.aims.logic.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.InputStream;

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

    /**
     * 读取json配置文件为JSONObject
     *
     * @param path 资源文件相对路径
     * @return 文件内容转换后的json对象
     */
    public static JSONObject readJsonFile(String path) {
        JSONObject json;
        InputStream stream = new Object() {
            public InputStream getInputStream(String path) {
                return this.getClass().getResourceAsStream(path);
            }
        }.getInputStream(path);
        if (stream == null) {
            throw new RuntimeException("读取文件失败");
        } else {
            json = JSON.parseObject(stream, JSONObject.class);
        }
        return json;
    }
}
