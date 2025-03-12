package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicLogEntity;

public interface LogicLogService extends BaseService<LogicLogEntity, String> {
    void clearLog();

    /**
     * 删除指定天数前的日志
     *
     * @param days 天数
     */
    void deleteLogBeforeDays(int days);
}
