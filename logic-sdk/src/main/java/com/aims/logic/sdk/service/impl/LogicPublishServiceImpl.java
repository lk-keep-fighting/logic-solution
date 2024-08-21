package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.service.LogicPublishService;
import org.springframework.stereotype.Service;

@Service
public class LogicPublishServiceImpl extends BaseServiceImpl<LogicPublishedEntity, Long> implements LogicPublishService {

    public LogicPublishServiceImpl() {
        this.entityClass = new LogicPublishedEntity().getClass();
    }
}