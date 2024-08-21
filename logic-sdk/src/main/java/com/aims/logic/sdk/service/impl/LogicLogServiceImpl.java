package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LogicLogService;
import org.springframework.stereotype.Service;

@Service
public class LogicLogServiceImpl extends BaseServiceImpl<LogicLogEntity, String> implements LogicLogService {

    public LogicLogServiceImpl() {
        this.entityClass = new LogicLogEntity().getClass();
    }

    public void clearLog() {
        jdbcTemplate.update("truncate logic_log");
    }
}