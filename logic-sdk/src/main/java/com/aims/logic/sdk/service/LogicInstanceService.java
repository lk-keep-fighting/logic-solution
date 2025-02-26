package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicInstanceEntity;

import java.util.List;

public interface LogicInstanceService extends BaseService<LogicInstanceEntity, String> {
    LogicInstanceEntity getInstance(String logicId, String bizId);

    List<LogicInstanceEntity> queryLongtimeRunningBiz(int timeout);

    int deleteCompletedBizInstanceByLogicId(String logicId);

    int deleteCompletedBizInstance();

    int updateInstanceNextId(String logicId, String bizId, String nextId, String nextName, String varsJsonEnd);
}
