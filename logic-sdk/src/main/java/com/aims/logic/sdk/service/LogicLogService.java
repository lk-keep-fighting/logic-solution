package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicLogEntity;

public interface LogicLogService extends BaseService<LogicLogEntity> {
    void clearLog();
}
