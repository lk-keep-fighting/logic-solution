package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicInstanceEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface LogicInstanceService extends BaseService<LogicInstanceEntity, String> {
    LogicInstanceEntity getInstance(String logicId, String bizId);

    List<LogicInstanceEntity> queryLongtimeRunningBiz(int timeout);

    List<LogicInstanceEntity> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning);

    List<LogicInstanceEntity> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess);

    int deleteCompletedBizInstanceByLogicId(String logicId);

    int deleteCompletedBizInstance();

    int updateInstanceNextId(String logicId, String bizId, String nextId, String nextName, String varsJsonEnd);
}
