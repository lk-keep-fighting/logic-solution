package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LogicInstanceServiceImpl extends BaseServiceImpl<LogicInstanceEntity, String> implements LogicInstanceService {

    public LogicInstanceServiceImpl() {
    }

    @Override
    public LogicInstanceEntity getInstance(String logicId, String bizId) {
        if (logicId == null || bizId == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select * from logic_instance where logicId = '").append(logicId);
        sql.append("' and bizId = '").append(bizId).append("' order by version desc limit 1");
        try {
            return jdbcTemplate.queryForObject(sql.toString(), new BeanPropertyRowMapper<>(LogicInstanceEntity.class));
        } catch (Exception e) {
            log.warn("获取实例失败，getInstance error: {},logicId:{},bizId:{}", e.getMessage(), logicId, bizId);
            return null;
        }
    }

    @Override
    public List<LogicInstanceEntity> queryLongtimeRunningBiz(int timeout) {
        String sql = String.format("SELECT * FROM logic_instance WHERE isRunning=true AND startTime<=NOW()-INTERVAL %s SECOND", timeout);
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LogicInstanceEntity.class));
        } catch (Exception e) {
            log.warn("获取实例失败，queryLongtimeRunningBiz error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<LogicInstanceEntity> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning) {
        String sql = null;
        if (isRunning == null) isRunning = false;
        if (createTimeFrom == null && createTimeTo == null)
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false and isRunning=%s", isRunning);
        else if (createTimeFrom == null)
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false and isRunning=%s AND createTime <= '%s'", isRunning, createTimeTo);
        else if (createTimeTo == null)
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false and isRunning=%s AND createTime >= '%s'", isRunning, createTimeFrom);
        else
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false and isRunning=%s AND createTime between '%s' AND  '%s'", isRunning, createTimeFrom, createTimeTo);
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LogicInstanceEntity.class));
        } catch (Exception e) {
            log.warn("获取实例失败，queryUncompletedBiz error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public int deleteCompletedBizInstanceByLogicId(String logicId) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from logic_instance where logicId = '").append(logicId)
                .append("' and isOver = true");
        return jdbcTemplate.update(sql.toString());
    }

    @Override
    public int deleteCompletedBizInstance() {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from logic_instance where isOver = true");
        return jdbcTemplate.update(sql.toString());
    }

    @Override
    public int updateInstanceNextId(String logicId, String bizId, String nextId, String nextName, String varsJsonEnd) {
        if (logicId == null || bizId == null) {
            throw new RuntimeException("logicId or bizId is null");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("update logic_instance set nextId='%s',nextName='%s',varsJsonEnd='%s',isOver=0 where logicId = '%s' and bizId = '%s'", nextId, nextName, varsJsonEnd, logicId, bizId));
        return jdbcTemplate.update(sql.toString());
    }
}