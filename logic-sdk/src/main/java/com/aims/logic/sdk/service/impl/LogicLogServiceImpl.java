package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.service.LogicLogService;
import org.springframework.stereotype.Service;

@Service
public class LogicLogServiceImpl extends BaseServiceImpl<LogicLogEntity, String> implements LogicLogService {

    public LogicLogServiceImpl() {
    }
    @Override
    public void clearLog() {
        jdbcTemplate.update("truncate logic_log");
    }

    @Override
    public void deleteLogBeforeDays(int days) {
        jdbcTemplate.update("delete from logic_log where serverTime < now() - interval ? day", days);
    }
}