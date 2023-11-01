package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.mapper.LogicBakMapper;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.service.LogicInstanceService;
import org.springframework.stereotype.Service;

@Service
public class LogicBakServiceImpl extends BaseServiceImpl<LogicBakMapper, LogicBakEntity> implements LogicBakService {
}