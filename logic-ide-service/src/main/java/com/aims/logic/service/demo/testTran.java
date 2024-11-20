package com.aims.logic.service.demo;

import com.aims.logic.service.demo.dto.TestDtoWithDetail;
import com.aims.logic.service.demo.entity.TestDetailEntity;
import com.aims.logic.service.demo.entity.TestEntity;
import com.aims.logic.service.demo.mapper.TestDetailMapper;
import com.aims.logic.service.demo.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class testTran {
    @Autowired
    TestMapper testMapper;
    @Autowired
    TestDetailMapper testDetailMapperMapper;

    public int insert(String id) {
        return testMapper.insert(new TestEntity().setId(id));
    }

    public int update(String id, String name) {
        return testMapper.updateById(new TestEntity().setId(id).setName(name));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TestEntity get(String id, String name, int sleepMillis) throws InterruptedException {
        var res = testMapper.selectById(id);
        System.out.println(id + "--before update:" + res.getName());
        testMapper.updateById(new TestEntity().setId(id).setName(name));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, null, null);
        executor.execute(() -> {
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(id + "--after update:" + testMapper.selectById(id).getName());
        });
        return testMapper.selectById(id);
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
