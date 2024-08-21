package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicInstanceEntity;

public interface LogicInstanceService extends BaseService<LogicInstanceEntity, String> {
    LogicInstanceEntity getInstance(String logicId, String bizId);

    int deleteCompletedBizInstanceByLogicId(String logicId);

    int deleteCompletedBizInstance();
}
