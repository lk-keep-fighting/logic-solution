package com.aims.logic.sdk.service.impl;

import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.event.LogicRunnerEventListener;
import com.aims.logic.sdk.service.LogicInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LogicInstanceServiceImpl extends BaseServiceImpl<LogicInstanceEntity, String> implements LogicInstanceService {


    private List<LogicRunnerEventListener> eventListener;

    public LogicInstanceServiceImpl(
            List<LogicRunnerEventListener> _eventListener
    ) {
        this.eventListener = _eventListener;
    }

    @Override
    public LogicInstanceEntity getInstance(String logicId, String bizId) {
        if (logicId == null || bizId == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select * from logic_instance where logicId = '").append(logicId);
        sql.append("' and bizId = '").append(bizId).append("' order by serverTime desc limit 1");
        try {
            return jdbcTemplate.queryForObject(sql.toString(), new BeanPropertyRowMapper<>(LogicInstanceEntity.class));
        } catch (Exception e) {
            log.info("未查询到实例，logicId:{},bizId:{}", logicId, bizId);
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
        return queryUncompletedBiz(createTimeFrom, createTimeTo, isRunning, null);
    }

    @Override
    public List<LogicInstanceEntity> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess) {
        return queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, null, null);
    }

    @Override
    public List<LogicInstanceEntity> queryUncompletedBizExclude(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess, Integer maxRetryTimes, List<String> excludeLogicIds) {
        String sql = null;
        if (createTimeFrom == null && createTimeTo == null)
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false ");
        else if (createTimeFrom == null)
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false AND createTime <= '%s'", createTimeTo);
        else if (createTimeTo == null)
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false AND createTime >= '%s'", createTimeFrom);
        else
            sql = String.format("SELECT * FROM logic_instance WHERE isOver=false AND createTime between '%s' AND  '%s'", createTimeFrom, createTimeTo);
        if (isRunning != null) {
            sql += String.format(" and isRunning=%s", isRunning);
        }
        if (isSuccess != null) {
            sql += String.format(" and success=%s", isSuccess);
        }
        if (maxRetryTimes != null)
            sql += String.format(" and retryTimes<=%s", maxRetryTimes);
        if (excludeLogicIds != null && !excludeLogicIds.isEmpty()) {
            sql += String.format(" and logicId not in ('%s')", String.join("','", excludeLogicIds));
        }
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

    /**
     * 删除业务实例
     * 为了避免清空表，时间区间和ids至少有一个值，否则无法执行
     *
     * @param createTimeFrom
     * @param createTimeTo
     * @param ids
     * @return
     */
    @Override
    public int deleteBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> ids) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from logic_instance where 1=1");
        StringBuilder condition = new StringBuilder();
        if (createTimeFrom != null && createTimeTo != null) {
            condition.append(String.format(" and createTime between '%s' AND  '%s'", createTimeFrom, createTimeTo));
        }
        if (ids != null && !ids.isEmpty()) {
            condition.append(String.format(" and id in ('%s')", String.join("','", ids)));
        }
        if (!condition.isEmpty()) {
            sql.append(condition);
        } else {
            throw new RuntimeException("时间区间和ids都为空！");
        }
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

    @Override
    public void triggerBizCompleted(LogicLog logicLog) {
        try {
            for (LogicRunnerEventListener eventListener : this.eventListener) {
                eventListener.onBizCompleted(logicLog.getLogicId(), logicLog.getBizId(), logicLog.getReturnData());
            }
        } catch (Exception e) {
            log.warn("触发业务完成事件失败，triggerBizCompleted error: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void triggerAfterLogicStop(LogicLog logicLog) {
        try {
            for (LogicRunnerEventListener eventListener : this.eventListener) {
                eventListener.afterLogicStop(logicLog.getLogicId(), logicLog.getBizId(), logicLog.getReturnData());
            }
        } catch (Exception e) {
            log.warn("触发逻辑运行开始事件失败，triggerBeforeLogicRun error: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void triggerBeforeLogicRun(LogicLog logicLog) {
        try {
            for (LogicRunnerEventListener eventListener : this.eventListener) {
                eventListener.beforeLogicRun(logicLog.getLogicId(), logicLog.getBizId(), logicLog.getParamsJson());
            }
        } catch (Exception e) {
            log.warn("触发逻辑运行开始事件失败，triggerBeforeLogicRun error: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}