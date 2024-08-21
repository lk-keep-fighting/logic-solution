package com.aims.logic.testsuite.orm;

import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.service.LogicPublishService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class testCrud {

    @Autowired
    LogicPublishService logicPublishService;

    @Test
    public void contextLoads() {
        var id = logicPublishService.insertAndGetId(new LogicPublishedEntity().setLogicId("dsadf"));
        System.out.println(id);
    }
}
