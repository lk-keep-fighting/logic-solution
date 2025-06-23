package com.aims.logic.sdk.service;

import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.sdk.entity.LogicInstanceEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface LogicInstanceService extends BaseService<LogicInstanceEntity, String> {
    LogicInstanceEntity getInstance(String logicId, String bizId);

    List<LogicInstanceEntity> queryLongtimeRunningBiz(int timeout);

    List<LogicInstanceEntity> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning);

    List<LogicInstanceEntity> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess);

    List<LogicInstanceEntity> queryUncompletedBizExclude(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess, Integer maxRetryTimes, List<String> excludeLogicIds);

    int deleteCompletedBizInstanceByLogicId(String logicId);

    int deleteCompletedBizInstance();

    int deleteBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> ids);

    int updateInstanceNextId(String logicId, String bizId, String nextId, String nextName, String varsJsonEnd);

    void triggerBizCompleted(LogicLog logicLog);

    void triggerAfterLogicStop(LogicLog logicLog);

    void triggerBeforeLogicRun(LogicLog logicLog);
}
