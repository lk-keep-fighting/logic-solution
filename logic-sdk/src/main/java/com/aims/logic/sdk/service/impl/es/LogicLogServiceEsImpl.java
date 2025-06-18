package com.aims.logic.sdk.service.impl.es;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LogicLogService;
import com.aims.logic.sdk.service.impl.BaseEsServiceImpl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//@Component
//@ConditionalOnLogicLogService("es")
public class LogicLogServiceEsImpl extends BaseEsServiceImpl<LogicLogEntity, String> implements LogicLogService {

    @Override
    public void clearLog() {
        // 构建OkHttpClient
        OkHttpClient client = new OkHttpClient();

        try {
            // 构建DELETE请求
            Request request = new Request.Builder()
                    .url(esHost + "/" + indexName)
                    .delete()
                    .build();

            // 执行请求
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("清除ES日志失败: " + response.body().string());
            }

        } catch (Exception e) {
            throw new RuntimeException("清除ES日志时发生错误", e);
        }
    }

    @Override
    public void deleteLogBeforeDays(int days) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteLogBeforeDays'");
    }
}