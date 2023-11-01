package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.service.LogicLogService;
import org.springframework.stereotype.Service;

@Service
public class LogicLogServiceImpl extends BaseServiceImpl<LogicLogMapper, LogicLogEntity> implements LogicLogService {
}