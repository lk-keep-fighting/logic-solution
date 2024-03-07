package com.aims.logic.service.demo.mapper;

import com.aims.logic.service.demo.entity.TestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TestMapper extends BaseMapper<TestEntity> {
}
