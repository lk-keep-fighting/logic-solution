package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dto.LongtimeRunningBizDto;
import com.aims.logic.runtime.contract.dto.UnCompletedBizDto;
import com.aims.logic.runtime.store.LogicConfigStoreService;
import com.aims.logic.sdk.LogicDataService;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.util.lock.BizLock;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogicDataServiceImpl implements LogicDataService {

    private LogicInstanceService insService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BizLock bizLock;
    @Autowired
    LogicBakService logicBakService;
    @Autowired
    LogicConfigStoreService logicConfigStoreService;


    public LogicDataServiceImpl(
            LogicInstanceService _insService) {
        insService = _insService;
    }

    @Override
    public Page<LogicInstanceEntity> queryBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> bizIds, long pageNum, long pageSize) {
        StringBuilder sql = new StringBuilder("select * from logic_instance where 1=1 ");
        StringBuilder condition = new StringBuilder();
        if (createTimeFrom != null) {
            condition.append(String.format(" and createTime >= '%s'", createTimeFrom));
        }
        if (createTimeTo != null) {
            condition.append(String.format(" and createTime <= '%s'", createTimeTo));
        }
        if (bizIds != null && !bizIds.isEmpty()) {
            condition.append(String.format(" and bizId in ('%s')", String.join("','", bizIds)));
        }
        sql.append(condition);
        if (pageSize != 0) {
            sql.append(String.format(" limit %d,%d", (pageNum - 1) * pageSize, pageSize));
        }
        var list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(LogicInstanceEntity.class));
        var countSql = String.format("select count(*) from logic_instance ");
        if (!condition.isEmpty()) {
            countSql += "where 1=1 " + condition;
        }
        var count = jdbcTemplate.queryForObject(countSql, Long.class);
        var p = new Page<LogicInstanceEntity>();
        p.setCurrent(pageNum);
        p.setSize(pageSize);
        p.setTotal(count);
        p.setRecords(list);
        return p;
    }

    @Override
    public List<LongtimeRunningBizDto> queryLongtimeRunningBiz(int timeout) {
        var list = insService.queryLongtimeRunningBiz(timeout);
        if (list == null)
            return null;
        return list.stream().map(insEntity -> new LongtimeRunningBizDto()
                .setLogicId(insEntity.getLogicId())
                .setBizId(insEntity.getBizId())
                .setStartTime(insEntity.getStartTime())
                .setIsAsync(insEntity.getIsAsync())
                .setParentBizId(insEntity.getParentBizId())
                .setParentLogicId(insEntity.getParentLogicId())).collect(Collectors.toList());

    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning) {
        return queryUncompletedBiz(createTimeFrom, createTimeTo, isRunning, null);
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess) {
        return queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, null, null);
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBizExclude(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess, Integer maxRetryTimes, List<String> excludeLogicIds) {
        var list = insService.queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, maxRetryTimes, excludeLogicIds);
        if (list == null)
            return null;

        return list.stream().map(insEntity -> new UnCompletedBizDto()
                .setLogicId(insEntity.getLogicId())
                .setBizId(insEntity.getBizId())
                .setCreateTime(insEntity.getCreateTime())
                .setIsRunning(insEntity.getIsRunning())
                .setIsBizLocked(bizLock.isBizLocked(insEntity.getLogicId(), insEntity.getBizId()))
                .setIsSuccess(insEntity.getSuccess())
                .setIsAsync(insEntity.getIsAsync())
                .setParentLogicId(insEntity.getParentLogicId())
                .setRetryTimes(insEntity.getRetryTimes())
                .setParentBizId(insEntity.getParentBizId())).collect(Collectors.toList());
    }

    @Override
    public int updateBizRetryTimes(String logicId, String bizId, int retryTimes) {
        return jdbcTemplate.update("update logic_instance set retryTimes = ? where logicId = ? and bizId = ?", retryTimes, logicId, bizId);
    }

    @Override
    public LogicInstanceEntity getBiz(String logicId, String bizId) {
        return insService.getInstance(logicId, bizId);
    }

    @Override
    public int deleteBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> ids) {
        return insService.deleteBiz(createTimeFrom, createTimeTo, ids);
    }

    @Override
    public LogicTreeNode tryGetLogicConfigByAllWays(String logicId, String version) {
        // 从备份表读取逻辑配置
        var logicBakEntityEntity = logicBakService.getByIdAndVersion(logicId, version);
        if (logicBakEntityEntity != null) {
            var bakConfig = logicBakEntityEntity.getConfigJson();
            return JSON.isValid(bakConfig) ? JSON.parseObject(bakConfig, LogicTreeNode.class) : null;
        }

        // 从当前运行的配置中获取逻辑配置，并匹配版本
        var config = logicConfigStoreService.readLogicConfigFromFile(logicId);
        if (config != null) {
            LogicTreeNode res = config.to(LogicTreeNode.class);
            if (version.equals(res.getVersion())) {
                logicBakService.insert(new LogicBakEntity().setId(logicId).setName(config.getString("name")).setVersion(version).setConfigJson(config.toJSONString()));
                return res;
            }
        }
        // version=null，读取最新配置
        config = logicConfigStoreService.readLogicConfigFromHost(logicId, null);
        if (config != null) {
            LogicTreeNode res = config.to(LogicTreeNode.class);
            if (version.equals(res.getVersion())) {
                logicBakService.insert(new LogicBakEntity().setId(logicId).setName(config.getString("name")).setVersion(version).setConfigJson(config.toJSONString()));
                return res;
            }
        }

        return null;
    }

}
