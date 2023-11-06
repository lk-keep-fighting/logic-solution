package com.aims.logic.sdk.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author liukun
 */
public class FileUtil {
    /**
     * 配置文件根目录
     */
    private static String configDir = "";

    public static String getConfigDir() {
        if (Objects.equals(configDir, "")) {
            configDir = SpringContextUtil.getApplicationContext().getEnvironment().getProperty("logic.config-dir");
        }
        if (configDir != null && !configDir.isBlank()) {
            return configDir;
        } else {
            return buildPath(System.getProperty("user.dir"), "logic-configs");
        }
    }

    /**
     * 读取json配置文件为JSONObject
     *
     * @param dir      资源文件目录
     * @param fileName 资源文件名
     * @return 文件内容转换后的json对象
     */
    public static JSONObject readJsonFile(String dir, String fileName) {
        JSONObject json;
        String fullDir = buildPath(getConfigDir(), dir);
        String path = buildPath(fullDir, fileName);
        System.out.printf("read json file:%s%n", path);
        String jsonStr;
        try {
            jsonStr = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        json = JSON.parseObject(jsonStr, JSONObject.class);
        return json;
    }

    public static JSONObject readOrCreateFile(String dir, String fileName, String defContent) {
        JSONObject json;
        String fullDir = buildPath(getConfigDir(), dir);
        String path = buildPath(fullDir, fileName);
        System.out.printf("read json file:%s%n", path);
        String jsonStr;
        createDirIfNotExist(fullDir);
        try {
            File file = new File(path);
            if (file.exists()) {
                jsonStr = Files.readString(Path.of(path));
            } else {
                writeFile(dir, fileName, defContent);
                jsonStr = defContent;
            }
        } catch (
                Exception e) {
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

    public static void createDirIfNotExist(String dir) {
        File dirObj = new File(dir);
        if (!dirObj.exists()) {
            var res = dirObj.mkdirs();
            if (!res) {
                throw new RuntimeException("创建文件夹失败!" + dir);
            }
        }
    }

    /**
     * 写文件，会根据调试环境和jar包运行环境自动切换
     *
     * @param dir      文件目录
     * @param filename 文件名
     * @param content  文件内容
     * @throws Exception 异常
     */
    public static String writeFile(String dir, String filename, String content) throws Exception {
        var filePath = buildPath(buildPath(getConfigDir(), dir), filename);
        System.out.printf("save file:%s%n", filePath);
        createDirIfNotExist(dir);
        File file = new File(filePath);
        if (!file.exists()) {
            var res = file.createNewFile();
            if (!res) {
                throw new RuntimeException("创建文件失败！" + filePath);
            }
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return filePath;
    }

    /**
     * 是否jar运行环境
     *
     * @return 是否jar运行环境
     */
    public static boolean isJar() {
        URL url = FileUtil.class.getResource("");
        String protocol = null;
        if (url != null) {
            protocol = url.getProtocol();
        }
        return "jar".equals(protocol);
    }

    /**
     * 获取运行时目录
     * 调试是为项目目录，运行时为jar所在目录
     *
     * @return 返回路径
     */
    public static String getBasePath() {
        return System.getProperty("user.dir");
    }
}
