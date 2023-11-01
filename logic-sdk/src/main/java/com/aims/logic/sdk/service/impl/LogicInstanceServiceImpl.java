package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LogicInstanceServiceImpl extends BaseServiceImpl<LogicInstanceMapper, LogicInstanceEntity> implements LogicInstanceService {
}