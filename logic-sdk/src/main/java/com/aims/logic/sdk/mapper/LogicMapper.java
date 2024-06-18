package com.aims.logic.sdk.mapper;

import com.aims.logic.sdk.entity.LogicEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface LogicMapper extends BaseMapper<LogicEntity> {
    @Select("select distinct module from logic where module is not null")
    List<String> getModuleList();
}
