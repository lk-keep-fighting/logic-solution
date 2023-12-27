package com.aims.logic.service.demo;

import com.aims.logic.service.demo.entity.TestEntity;
import com.aims.logic.service.demo.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class testTran {
    @Autowired
    TestMapper testMapper;

    public int insert(String id) {
        return testMapper.insert(new TestEntity().setId(id));
    }

    /**
     * 测试部分插入成功是否会回滚
     * @param ids
     */
    public void insertOneByOne(String[] ids) {
        for (String id : ids) {
            testMapper.insert(new TestEntity().setId(id));
        }
    }
}
