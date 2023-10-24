package com.aims.logic.sdk.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.InputStream;

public class FileUtil {
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
