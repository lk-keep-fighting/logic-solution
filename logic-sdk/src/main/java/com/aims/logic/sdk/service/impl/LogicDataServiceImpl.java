package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.contract.dto.LongtimeRunningBizDto;
import com.aims.logic.runtime.contract.dto.UnCompletedBizDto;
import com.aims.logic.sdk.LogicDataService;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogicDataServiceImpl implements LogicDataService {
    private String logicInstanceDataModelId = "logic_instance";

    private LogicInstanceService logicInstanceService;
    private LogicInstanceService insService;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public LogicDataServiceImpl(LogicInstanceService _logicInstanceService,
                                LogicInstanceService _insService) {
        logicInstanceService = _logicInstanceService;
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
        return logicInstanceService.getInstance(logicId, bizId);
    }

    @Override
    public int deleteBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> ids) {
        return insService.deleteBiz(createTimeFrom, createTimeTo, ids);
    }
}
