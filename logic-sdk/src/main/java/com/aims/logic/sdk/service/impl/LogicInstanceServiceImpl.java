package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LogicInstanceServiceImpl extends BaseServiceImpl<LogicInstanceMapper, LogicInstanceEntity> implements LogicInstanceService {
    @Override
    public LogicInstanceEntity getInstance(String logicId, String bizId) {
        QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
        q.allEq(Map.of("logicId", logicId, "bizId", bizId));
        return this.getOne(q);
    }

//    @Override
//    public long clearCompletedInstanceOver(String logicId, String bizId) {
//        QueryWrapper<LogicInstanceEntity> q = new QueryWrapper<>();
//        q.allEq(Map.of("logicId", logicId, "bizId", bizId, "isOver", true));
//        return this.remove(q);
//    }
}