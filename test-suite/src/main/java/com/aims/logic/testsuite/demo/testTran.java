package com.aims.logic.testsuite.demo;

import com.aims.logic.runtime.LogicBizException;
import com.aims.logic.sdk.annotation.LogicItem;
import com.aims.logic.testsuite.demo.dto.TestDtoWithDetail;
import com.aims.logic.testsuite.demo.entity.TestAutoIdEntity;
import com.aims.logic.testsuite.demo.entity.TestDetailEntity;
import com.aims.logic.testsuite.demo.entity.TestEntity;
import com.aims.logic.testsuite.demo.mapper.TestAutoIdMapper;
import com.aims.logic.testsuite.demo.mapper.TestDetailMapper;
import com.aims.logic.testsuite.demo.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class testTran {
    @Autowired
    TestMapper testMapper;
    @Autowired
    TestAutoIdMapper testAutoIdMapper;
    @Autowired
    TestDetailMapper testDetailMapperMapper;

    //    @Transactional(rollbackFor = Exception.class)
    @LogicItem(name = "插入测试", group = "测试事务", memo = "很简单的插入id值，用于测试插入id重复时报错是否会回滚上游事务")
    public int insert(String id) {
        return testMapper.insert(new TestEntity().setId(id));
    }

    @LogicItem(name = "根据id读取", group = "测试事务", memo = "")
    public TestEntity getById(String id) {
        return testMapper.selectById(id);
    }

    @LogicItem(name = "根据id删除-与插入同类", group = "测试事务", memo = "")
    public int deleteById(String id) {
        return testMapper.deleteById(id);
    }

    public int insertDto(TestEntity entity) {
        return testMapper.insert(entity);
    }

    public int insertDtoList(List<TestEntity> entitys) {
        for (var entity : entitys) {
            testMapper.insert(entity);
        }
        return 1;
    }

    public int insertTestAutoId(TestAutoIdEntity entity) {
        return testAutoIdMapper.insert(entity);
    }

    public int insertDtoArray(TestEntity[] entitys) {
        for (var entity : entitys) {
            testMapper.insert(entity);
        }
        return 1;
    }

    public int insertDtoWithDetail(TestDtoWithDetail dto) {
        TestEntity testEntity = new TestEntity()
                .setId(dto.getId())
                .setName(dto.getName());
        testMapper.insert(testEntity);
        for (var d : dto.getDetailEntityList()) {
            TestDetailEntity testDetailEntity = new TestDetailEntity()
                    .setTestId(dto.getId()).setId(d.getId()).setName(d.getName());
            testDetailMapperMapper.insert(testDetailEntity);
        }
        return 1;
    }

    /**
     * 测试部分插入成功是否会回滚
     *
     * @param ids
     */
    @LogicItem(name = "测试部分插入成功是否会回滚", group = "测试事务", memo = "")
    public void insertOneByOne(String[] ids) {
        for (String id : ids) {
            testMapper.insert(new TestEntity().setId(id));
        }
    }

    public void throwBizError(String msg) {
        throw new LogicBizException(msg);
    }
}
