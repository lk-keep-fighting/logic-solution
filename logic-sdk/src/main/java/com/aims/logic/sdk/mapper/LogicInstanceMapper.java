package com.aims.logic.sdk.mapper;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LogicInstanceMapper extends BaseMapper<LogicInstanceEntity> {
}
