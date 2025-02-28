package com.aims.logic.testsuite.demo;

import com.aims.logic.sdk.annotation.LogicItem;
import com.aims.logic.testsuite.demo.mapper.TestAutoIdMapper;
import com.aims.logic.testsuite.demo.mapper.TestDetailMapper;
import com.aims.logic.testsuite.demo.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class testTran2 {
    @Autowired
    TestMapper testMapper;
    @Autowired
    TestAutoIdMapper testAutoIdMapper;
    @Autowired
    TestDetailMapper testDetailMapperMapper;

    @LogicItem(name = "根据id删除", group = "测试事务", memo = "")
    public int deleteById(String id) {
        return testMapper.deleteById(id);
    }
}
