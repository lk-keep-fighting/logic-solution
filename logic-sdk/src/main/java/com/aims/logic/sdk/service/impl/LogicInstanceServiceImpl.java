package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.service.LogicInstanceService;
import com.aims.logic.sdk.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogicInstanceServiceImpl extends BaseServiceImpl<LogicInstanceEntity, String> implements LogicInstanceService {

    public LogicInstanceServiceImpl() {
    }

    @Override
    public LogicInstanceEntity getInstance(String logicId, String bizId) {
        if (logicId == null || bizId == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select * from logic_instance where logicId = '").append(logicId);
        sql.append("' and bizId = '").append(bizId).append("' order by version desc limit 1");
        try {
            var res = jdbcTemplate.queryForMap(sql.toString());
            return MapUtils.mapToBean(res, LogicInstanceEntity.class);
        } catch (Exception e) {
            log.warn("获取实例失败，getInstance error: {},logicId:{},bizId:{}", e.getMessage(), logicId, bizId);
            return null;
        }
    }

    @Override
    public int deleteCompletedBizInstanceByLogicId(String logicId) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from logic_instance where logicId = '").append(logicId)
                .append("' and isOver = true");
        return jdbcTemplate.update(sql.toString());
    }

    @Override
    public int deleteCompletedBizInstance() {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from logic_instance where isOver = true");
        return jdbcTemplate.update(sql.toString());
    }

}