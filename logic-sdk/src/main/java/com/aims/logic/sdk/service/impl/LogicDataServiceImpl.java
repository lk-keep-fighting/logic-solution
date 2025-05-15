package com.aims.logic.sdk.service.impl;

import com.aims.datamodel.core.dsl.DataModel;
import com.aims.datamodel.core.dsl.DataViewCondition;
import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.runtime.contract.dto.LongtimeRunningBizDto;
import com.aims.logic.runtime.contract.dto.UnCompletedBizDto;
import com.aims.logic.sdk.LogicDataService;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogicDataServiceImpl implements LogicDataService {
    private String logicInstanceDataModelId = "logic_instance";

    private LogicInstanceService logicInstanceService;
    private LogicInstanceService insService;


    public LogicDataServiceImpl(LogicInstanceService _logicInstanceService,
                                LogicInstanceService _insService) {
        logicInstanceService = _logicInstanceService;
        insService = _insService;
    }

    @Override
    public Page<LogicInstanceEntity> queryBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, long pageNum, long pageSize) {
        QueryInput input = new QueryInput();
        input.setFrom(new DataModel().setMainTable(logicInstanceDataModelId));
        List<DataViewCondition> cons = new ArrayList<>();
        DataViewCondition c = new DataViewCondition();
        c.setColumn("createTime");
        c.setOperator("BETWEEN");
        c.setValues(List.of(createTimeFrom, createTimeTo));
        cons.add(c);
        input.setConditions(cons);
        return logicInstanceService.queryPageByInput(input);
    }

    @Override
    public List<LongtimeRunningBizDto> queryLongtimeRunningBiz(int timeout) {
        var list = insService.queryLongtimeRunningBiz(timeout);
        if (list == null)
            return null;
        return list.stream().map(insEntity -> new LongtimeRunningBizDto()
                .setLogicId(insEntity.getLogicId())
                .setBizId(insEntity.getBizId())
                .setStartTime(insEntity.getStartTime())
                .setIsAsync(insEntity.getIsAsync())
                .setParentBizId(insEntity.getParentBizId())
                .setParentLogicId(insEntity.getParentLogicId())).collect(Collectors.toList());

    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning) {
        return queryUncompletedBiz(createTimeFrom, createTimeTo, isRunning, null);
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess) {
        return queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, null);
    }

    @Override
    public List<UnCompletedBizDto> queryUncompletedBizExclude(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess, List<String> excludeLogicIds) {
        var list = insService.queryUncompletedBizExclude(createTimeFrom, createTimeTo, isRunning, isSuccess, excludeLogicIds);
        if (list == null)
            return null;
        return list.stream().map(insEntity -> new UnCompletedBizDto()
                .setLogicId(insEntity.getLogicId())
                .setBizId(insEntity.getBizId())
                .setCreateTime(insEntity.getCreateTime())
                .setIsRunning(insEntity.getIsRunning())
                .setIsSuccess(insEntity.getSuccess())
                .setIsAsync(insEntity.getIsAsync())
                .setParentLogicId(insEntity.getParentLogicId())
                .setParentBizId(insEntity.getParentBizId())).collect(Collectors.toList());
    }
}
