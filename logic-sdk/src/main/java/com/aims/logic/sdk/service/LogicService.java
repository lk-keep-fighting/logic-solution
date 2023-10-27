package com.aims.logic.sdk.service;

import com.aims.logic.sdk.mapper.LogicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogicService {
    private final LogicMapper logicMapper;

    @Autowired
    public LogicService(LogicMapper _logicMapper) {
        this.logicMapper = _logicMapper;
    }

}
