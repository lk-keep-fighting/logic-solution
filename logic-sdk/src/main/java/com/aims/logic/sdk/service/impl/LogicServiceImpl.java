package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.mapper.LogicBakMapper;
import com.aims.logic.sdk.mapper.LogicMapper;
import com.aims.logic.sdk.mapper.LogicPublishedMapper;
import com.aims.logic.sdk.service.LogicService;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author liukun
 */
@Service
@Log4j2
public class LogicServiceImpl extends BaseServiceImpl<LogicMapper, LogicEntity> implements LogicService {
    @Autowired
    private LogicMapper logicMapper;
    @Autowired
    private LogicBakMapper logicBakMapper;
    @Autowired
    private LogicPublishedMapper logicPublishedMapper;

    /**
     * 编辑逻辑，并向logic_bak插入备份数据
     *
     * @param id    逻辑编号
     * @param input 更新的字段，为null则不更新
     * @return 更新行数
     */
    @Override
    public int editAndBak(String id, LogicEntity input) {
        UpdateWrapper<LogicEntity> wrapper = new UpdateWrapper<LogicEntity>()
                .eq("id", id)
                .set("updateTime", input.getUpdateTime() == null ? LocalDateTime.now() : input.getUpdateTime());
        if (input.getName() != null) {
            wrapper.set("name", input.getName());
        }
        if (input.getModule() != null) {
            wrapper.set("module", input.getModule());
        }
        if (input.getVersion() != null) {
            wrapper.set("version", input.getVersion());
        }
        if (input.getConfigJson() != null) {
            wrapper.set("configJson", input.getConfigJson());
        }
        var uptRows = logicMapper.update(null, wrapper);
        if (uptRows > 0) {
            var fullLogicEntity = logicMapper.selectById(id);
            LogicBakEntity bak = new LogicBakEntity();
            bak.setId(fullLogicEntity.getId());
            bak.setName(fullLogicEntity.getName());
            bak.setModule(fullLogicEntity.getModule());
            bak.setVersion(fullLogicEntity.getVersion());
            bak.setConfigJson(fullLogicEntity.getConfigJson());
            bak.setUpdateTime(fullLogicEntity.getUpdateTime());
            logicBakMapper.insert(bak);
        }
        return uptRows;
    }


    @Override
    public String pubToLocal(String id) {
        var logicEntity = baseMapper.selectById(id);
        if (logicEntity != null) {
            var config = logicEntity.getConfigJson();
            String path = RuntimeUtil.saveLogicConfigToFile(id, config);
            LogicPublishedEntity publishedEntity = new LogicPublishedEntity();
            publishedEntity.setLogicId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setPublishTime(LocalDateTime.now())
                    .setTarget("local");
            logicPublishedMapper.insert(publishedEntity);
            return path;
        } else {
            throw new RuntimeException("未找到逻辑：" + id);
        }
    }

    @Override
    public String pubToLocalFromEntityJson(String jsonStr, String source) {
        var logicEntity = JSONObject.parseObject(jsonStr, LogicEntity.class);
        if (logicEntity != null) {
            String path = RuntimeUtil.saveLogicConfigToFile(logicEntity.getId(), logicEntity.getConfigJson());
            LogicPublishedEntity publishedEntity = new LogicPublishedEntity();
            publishedEntity.setLogicId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setPublishTime(LocalDateTime.now())
                    .setSource(source)
                    .setTarget("file");
            logicEntity.insertOrUpdate();
            LogicBakEntity bak = new LogicBakEntity();
            bak.setId(logicEntity.getId())
                    .setName(logicEntity.getName())
                    .setModule(logicEntity.getModule())
                    .setVersion(logicEntity.getVersion())
                    .setConfigJson(logicEntity.getConfigJson())
                    .setUpdateTime(logicEntity.getUpdateTime());
            logicBakMapper.insert(bak);
            logicPublishedMapper.insert(publishedEntity);
            return path;
        } else {
            log.error("json格式有误");
            log.error(jsonStr);
            throw new RuntimeException("json格式有误");
        }
    }

    OkHttpClient client = new OkHttpClient();

    @Override
    public String pubToIdeHost(String id, String url) {
        var logicEntity = baseMapper.selectById(id);
        if (logicEntity != null) {
            var config = JSON.toJSONString(logicEntity);
            var body = RequestBody.create(config, MediaType.parse("application/json; charset=utf-8"));
            String idePubUrl = String.format("%s/api/ide/publish/logic/to-local-from-entity-json", url);
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
            logicPublishedMapper.insert(publishedEntity);
            return url;
        } else {
            log.error("未找到要发布的逻辑：" + id);
            throw new RuntimeException("未找到要发布的逻辑：" + id);
        }
    }
}
