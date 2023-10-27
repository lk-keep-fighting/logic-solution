package com.aims.logic.sdk.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    /**
     * 读取json配置文件为JSONObject
     *
     * @param dir      资源文件目录
     * @param fileName 资源文件名
     * @return 文件内容转换后的json对象
     */
    public static JSONObject readJsonFile(String dir, String fileName) {
        JSONObject json;
        String path = buildPath(buildPath(getRuntimePath(), dir), fileName);
        String jsonStr = null;
        try {
            jsonStr = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        json = JSON.parseObject(jsonStr, JSONObject.class);
        return json;
    }

    /**
     * 拼接文件全路径
     *
     * @param dir         文件夹
     * @param subFilePath 文件名
     * @return 完整地址
     */
    private static String buildPath(String dir, String subFilePath) {
        return dir + File.separator + subFilePath;
    }

    /**
     * 写文件，会根据调试环境和jar包运行环境自动切换
     *
     * @param dir      文件目录
     * @param filename 文件名
     * @param content  文件内容
     * @throws Exception 异常
     */
    public static void writeFile(String dir, String filename, String content) throws Exception {
        var dirPath = System.getProperty("user.dir");
        var filePath = buildPath(buildPath(dirPath, dir), filename);
        System.out.printf("save file:%s", filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * 是否jar运行环境
     *
     * @return 是否jar运行环境
     */
    public static boolean isJar() {
        URL url = FileUtil.class.getResource("");
        String protocol = url.getProtocol();
        return "jar".equals(protocol);
    }

    /**
     * 获取运行时目录
     * 调试是为项目目录，运行时为jar所在目录
     *
     * @return 返回路径
     */
    public static String getRuntimePath() {
        String path = System.getProperty("user.dir");
        return path;
    }
}
