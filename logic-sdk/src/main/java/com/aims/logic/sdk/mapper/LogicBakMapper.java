package com.aims.logic.sdk.mapper;

import com.aims.logic.sdk.entity.LogicBakEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LogicBakMapper extends BaseMapper<LogicBakEntity> {
}
