package com.aims.logic.testsuite;

import com.aims.logic.sdk.LogicDataService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@SpringBootTest
public class TestQuerySDK {
    @Autowired
    LogicDataService logic;

    @Test
    public void contextLoads() {
        var res = logic.queryUncompletedBiz(null, null, null);
        assert !res.isEmpty();
//        res = logic.queryUncompletedBiz(null, LocalDateTime.now(), null);
//        assert !res.isEmpty();
//        res = logic.queryUncompletedBiz(LocalDateTime.now().minusHours(1), LocalDateTime.now(), null);
//        assert !res.isEmpty();
//        res = logic.queryUncompletedBiz(LocalDateTime.now().minusMinutes(1), LocalDateTime.now(), false);
//        assert !res.isEmpty();
//        res = logic.queryUncompletedBiz(LocalDateTime.now().minusHours(1), LocalDateTime.now(), true);
//        assert !res.isEmpty();
        List<String> excludeLogicIds = null;// List.of("test.multisublogic", "loigc2");
        res = logic.queryUncompletedBizExclude(LocalDateTime.now().minusDays(60).minusHours(1), LocalDateTime.now(), null, null, -1, excludeLogicIds);
        assert res.isEmpty();

    }


    @Autowired
    LogicDataService logicDataService;

    @Test
    void testQuery() {
        List<String> bizIds = List.of("2", "3");
        var res = logicDataService.queryBiz(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1), bizIds, 1, 10);
        System.out.println(res);
    }
}
