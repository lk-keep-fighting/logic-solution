package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.service.LogicLogService;
import org.springframework.stereotype.Service;

@Service
public class LogicLogServiceImpl extends BaseServiceImpl<LogicLogMapper, LogicLogEntity> implements LogicLogService {

    public void clearLog() {
        this.baseMapper.clearLog();
    }
}