package com.aims.logic.testsuite.demo.mapper;

import com.aims.logic.testsuite.demo.entity.TestDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TestDetailMapper extends BaseMapper<TestDetailEntity> {
}
