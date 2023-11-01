package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.mapper.LogicPublishedMapper;
import com.aims.logic.sdk.service.LogicLogService;
import com.aims.logic.sdk.service.LogicPublishService;
import org.springframework.stereotype.Service;

@Service
public class LogicPublishServiceImpl extends BaseServiceImpl<LogicPublishedMapper, LogicPublishedEntity> implements LogicPublishService {
}