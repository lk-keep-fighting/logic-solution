package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@Service
public class LogicInstanceServiceImpl extends ServiceImpl<LogicInstanceMapper, LogicInstanceEntity> implements LogicInstanceService {
}