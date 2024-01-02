package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.mapper.LogicBakMapper;
import com.aims.logic.sdk.mapper.LogicMapper;
import com.aims.logic.sdk.mapper.LogicPublishedMapper;
import com.aims.logic.sdk.service.LogicService;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author liukun
 */
@Service
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
                    .setPublishTime(LocalDateTime.now());
            logicPublishedMapper.insert(publishedEntity);
            return path;
        } else {
            throw new RuntimeException("未找到逻辑：" + id);
        }
    }
}
