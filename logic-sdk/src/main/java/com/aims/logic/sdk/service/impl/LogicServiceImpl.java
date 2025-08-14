package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.service.LogicService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liukun
 */
@Service
@Log4j2
public class LogicServiceImpl extends BaseServiceImpl<LogicEntity, String> implements LogicService {

    public LogicBakService logicBakService;
    public LogicPublishServiceImpl logicPublishService;

    @Autowired
    public LogicServiceImpl(
            LogicBakService _logicBakService,
            LogicPublishServiceImpl _logicPublishService
    ) {
        logicBakService = _logicBakService;
        logicPublishService = _logicPublishService;
//        this.entityClass = new LogicEntity().getClass();
    }

    /**
     * 编辑逻辑，并向logic_bak插入备份数据
     *
     * @param id    逻辑编号
     * @param input 更新的字段，为null则不更新
     * @return 更新行数
     */
    @Override
    public int editAndBak(String id, LogicEntity input) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("updateTime", input.getUpdateTime() == null ? LocalDateTime.now() : input.getUpdateTime());
        if (input.getName() != null) {
            valuesMap.put("name", input.getName());
        }
        if (input.getModule() != null) {
            valuesMap.put("module", input.getModule());
        }
        if (input.getVersion() != null) {
            valuesMap.put("version", input.getVersion());
        }
        if (input.getConfigJson() != null) {
            valuesMap.put("configJson", input.getConfigJson());
        }
        var uptRows = this.updateById(id, valuesMap);
        if (uptRows > 0) {
            var fullLogicEntity = selectById(id);
            LogicBakEntity bak = new LogicBakEntity();
            bak.setId(fullLogicEntity.getId());
            bak.setName(fullLogicEntity.getName());
            bak.setModule(fullLogicEntity.getModule());
            bak.setVersion(fullLogicEntity.getVersion());
            bak.setConfigJson(fullLogicEntity.getConfigJson());
            bak.setUpdateTime(fullLogicEntity.getUpdateTime());
            logicBakService.insert(bak);
        }
        return uptRows;
    }


    @Override
    public String pubToLocal(String id, boolean isHotUpdate) {
        var logicEntity = selectById(id);
        if (logicEntity != null) {
            var config = logicEntity.getConfigJson();
            String path = RuntimeUtil.saveLogicConfigToFile(id, config, isHotUpdate);
            LogicPublishedEntity publishedEntity = new LogicPublishedEntity();
            publishedEntity.setLogicId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setPublishTime(LocalDateTime.now())
                    .setTarget("local");
            logicPublishService.insert(publishedEntity);
            return path;
        } else {
            throw new RuntimeException("未找到逻辑：" + id);
        }
    }

    @Override
    public String pubToLocalFromEntityJson(JSONObject jsonObject, String source, boolean isHotUpdate) {
        var logicEntity = jsonObject.to(LogicEntity.class);
        if (logicEntity != null) {
            String path = RuntimeUtil.saveLogicConfigToFile(logicEntity.getId(), logicEntity.getConfigJson(), isHotUpdate);
            LogicPublishedEntity publishedEntity = new LogicPublishedEntity();
            publishedEntity.setLogicId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setPublishTime(LocalDateTime.now())
                    .setSource(source)
                    .setTarget("file");
            this.removeById(logicEntity.getId());
            this.insert(logicEntity);
            LogicBakEntity bak = new LogicBakEntity();
            bak.setId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setUpdateTime(logicEntity.getUpdateTime());
            logicBakService.insert(bak);
            logicPublishService.insert(publishedEntity);
            return path;
        } else {
            log.error("发布的logicEntity为null");
            throw new RuntimeException("json格式有误");
        }
    }

    OkHttpClient client = new OkHttpClient();

    @Override
    public String pubToIdeHost(String id, String url, boolean isHotUpdate) {
        var logicEntity = selectById(id);
        if (logicEntity != null) {
            var config = JSON.toJSONString(logicEntity);
            var body = RequestBody.create(config, MediaType.parse("application/json; charset=utf-8"));
            String idePubUrl = String.format("%s/api/ide/publish/logic/to-local-from-entity-json?isHotUpdate=%s", url, isHotUpdate);
            Request req = new Request.Builder().url(idePubUrl).post(body).build();
            try (var rep = client.newCall(req).execute()) {
                if (!rep.isSuccessful()) {
                    log.error("请求异常，Http Code:" + rep.code() + ", " + rep.message());
                    throw new RuntimeException("请求异常，Http Code:" + rep.code() + ", " + rep.message());
                }
            } catch (IOException e) {
                log.error("请求catch异常:");
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            LogicPublishedEntity publishedEntity = new LogicPublishedEntity();
            publishedEntity.setLogicId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setPublishTime(LocalDateTime.now())
                    .setTarget(url);
            logicPublishService.insert(publishedEntity);
            return url;
        } else {
            log.error("未找到要发布的逻辑：" + id);
            throw new RuntimeException("未找到要发布的逻辑：" + id);
        }
    }

    @Override
    public Page<LogicEntity> selectPageFromRemoteIde(String ideHost, FormQueryInput input) {
        if (ideHost == null)
            throw new RuntimeException("远程地址不能为空");
        var queryBody = JSON.toJSONString(input);
        var body = RequestBody.create(queryBody, MediaType.parse("application/json; charset=utf-8"));
        String remoteIdeQueryUrl = String.format("%s/api/ide/logics", ideHost);
        Request req = new Request.Builder().url(remoteIdeQueryUrl).post(body).build();
        try (var rep = client.newCall(req).execute()) {
            if (!rep.isSuccessful()) {
                log.error("请求异常，Http Code:" + rep.code() + ", " + rep.message());
                throw new RuntimeException("请求异常，Http Code:" + rep.code() + ", " + rep.message());
            }
            if (rep.body() != null) {
                String repBody = rep.body().string();
                if (JSON.isValid(repBody)) {
                    var repData = JSON.parseObject(repBody);
                    return repData.getObject("data", new TypeReference<Page<LogicEntity>>() {
                    });
                } else {
                    log.error("请求异常，Http Code:" + rep.code() + ", " + rep.message());
                    throw new RuntimeException("请求异常，Http Code:" + rep.code() + ", " + rep.message());
                }
            }
        } catch (IOException e) {
            log.error("请求catch异常:");
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }


    @Override
    public List<Map<String, Object>> getModuleList() {
        List<Map<String, Object>> res = new ArrayList<>();
        var list = jdbcTemplate.queryForList("select distinct module from logic where module is not null");
        list.forEach(m -> {
            res.add(Map.of("label", m.get("module"), "id", m.get("module")));
        });
        return res;
    }
}
