package com.aims.logic.testsuite.demo;

import com.aims.logic.sdk.annotation.LogicItem;
import com.aims.logic.testsuite.demo.entity.TestEntity;
import com.aims.logic.testsuite.demo.exception.CustomException;
import com.aims.logic.testsuite.demo.mapper.TestAutoIdMapper;
import com.aims.logic.testsuite.demo.mapper.TestDetailMapper;
import com.aims.logic.testsuite.demo.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestTran2Service {
    @Autowired
    TestMapper testMapper;
    @Autowired
    TestAutoIdMapper testAutoIdMapper;
    @Autowired
    TestDetailMapper testDetailMapperMapper;

    @LogicItem(name = "插入测试2", group = "测试事务", memo = "很简单的插入id值，用于测试插入id重复时报错是否会回滚上游事务")
    public int insert(String id) {
        return testMapper.insert(new TestEntity().setId(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @LogicItem(name = "插入测试22（注解事务）", group = "测试事务", memo = "包含事务注解")
    public int insertWithTran(String id, boolean thorwError) {
        testMapper.insert(new TestEntity().setId(id));
        if (thorwError) {
            throw new CustomException("主动抛出业务异常");
        }
        return 1;
    }

    @LogicItem(name = "根据id删除", group = "测试事务", memo = "")
    public int deleteById(String id) {
        return testMapper.deleteById(id);
    }
}
