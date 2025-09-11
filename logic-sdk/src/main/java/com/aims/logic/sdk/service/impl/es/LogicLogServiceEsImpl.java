package com.aims.logic.sdk.service.impl.es;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LogicLogService;
import com.aims.logic.sdk.service.impl.BaseEsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public class LogicLogServiceEsImpl extends BaseEsServiceImpl<LogicLogEntity, String> implements LogicLogService {

    public LogicLogServiceEsImpl(OkHttpClient esHttpClient) {
        super(esHttpClient);
    }

    /**
     * 构建ES索引
     */
    private void initEsIndex() {
        try {
            deleteEsIndex();
        } catch (Exception e) {
            log.error("初始化时删除索引失败:" + e.getMessage());
            e.printStackTrace();
        }
        String bodyStr = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"bizId\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"env\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"host\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"id\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"isOver\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"itemLogs\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"logicId\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"messageId\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"paramsJson\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"returnData\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"serverTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"success\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"varsJson\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"varsJsonEnd\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"version\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        try {
            Request request = new Request.Builder()
                    .url(esHost + "/" + indexName)
                    .put(RequestBody.create(MediaType.parse("application/json"), bodyStr))
                    .build();

            // 执行请求
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("初始化日志失败: " + response.body().string());
            }

        } catch (Exception e) {
            throw new RuntimeException("初始化ES日志时发生错误", e);
        }
    }

    /**
     * 清除ES索引
     */
    private void deleteEsIndex() {

        try {
            // 构建DELETE请求
            Request request = new Request.Builder()
                    .url(esHost + "/" + indexName)
                    .delete()
                    .build();

            // 执行请求
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("清除ES日志失败: " + response.body().string());
            }

        } catch (Exception e) {
            throw new RuntimeException("清除ES日志时发生错误", e);
        }
    }

    @Override
    public void clearLog() {
        //初始化ES索引
        initEsIndex();

        try {
            // 构建DELETE请求
            Request request = new Request.Builder()
                    .url(esHost + "/" + indexName + "/_delete_by_query")
                    .post(RequestBody.create(MediaType.parse("application/json"), "{\"query\":{\"match_all\":{}}}}"))
                    .build();

            // 执行请求
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("清除ES日志失败: " + response.body().string());
            }

        } catch (Exception e) {
            throw new RuntimeException("清除ES日志时发生错误", e);
        }
    }

    @Override
    public void deleteLogBeforeDays(int days) {

        try {
            // 构建DELETE请求
            Request request = new Request.Builder()
                    .url(esHost + "/" + indexName + "/_delete_by_query")
                    .post(RequestBody.create(MediaType.parse("application/json"), "{\"query\":{\"range\":{\"createTime\":{\"lte\":\"now-" + days + "d/d\"}}}}"))
                    .build();

            // 执行请求
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("清除ES日志失败: " + response.body().string());
            }

        } catch (Exception e) {
            throw new RuntimeException("清除ES日志时发生错误", e);
        }
    }
}