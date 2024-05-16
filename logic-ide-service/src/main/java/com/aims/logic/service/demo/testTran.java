package com.aims.logic.service.demo;

import com.aims.logic.service.demo.dto.TestDtoWithDetail;
import com.aims.logic.service.demo.entity.TestDetailEntity;
import com.aims.logic.service.demo.entity.TestEntity;
import com.aims.logic.service.demo.mapper.TestDetailMapper;
import com.aims.logic.service.demo.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class testTran {
    @Autowired
    TestMapper testMapper;
    @Autowired
    TestDetailMapper testDetailMapperMapper;

    public int insert(String id) {
        return testMapper.insert(new TestEntity().setId(id));
    }

    public int throwError() {
        throw new RuntimeException("主动抛出RuntimeException");
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
    public void insertOneByOne(String[] ids) {
        for (String id : ids) {
            testMapper.insert(new TestEntity().setId(id));
        }
    }
}
