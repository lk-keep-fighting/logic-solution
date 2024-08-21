package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogicBakServiceImpl extends BaseServiceImpl<LogicBakEntity, String> implements LogicBakService {

    public LogicBakServiceImpl() {
//        this.entityClass = new LogicBakEntity().getClass();
    }

    @Override
    public LogicBakEntity getByIdAndVersion(String logicId, String version) {
        var res = jdbcTemplate.queryForMap("select * from logic_bak where id='" + logicId + "' and version='" + version + "' order by aid desc limit 1");
        try {
            return MapUtils.mapToBean(res, LogicBakEntity.class);
        } catch (Exception e) {
            log.error("getByIdAndVersion error", e);
            return null;
        }
    }
}