package com.aims.logic.sdk.mapper;

import com.aims.logic.sdk.entity.LogicLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LogicLogMapper extends BaseMapper<LogicLogEntity> {
    @Update("truncate logic_log")
    void clearLog();
}
