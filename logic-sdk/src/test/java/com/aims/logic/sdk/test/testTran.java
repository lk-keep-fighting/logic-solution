package com.aims.logic.sdk.test;

import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.mapper.LogicBakMapper;
import com.aims.logic.sdk.mapper.LogicMapper;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

//@Component
public class testTran {
//    @Autowired
    LogicMapper logicMapper;

    public String func1(JSONObject ctx, String script) {
        return script;
    }

    public String insertData(String id) {
        logicMapper.insert(new LogicEntity()
                .setId(id));
        return id;
    }
//    public String func1() {
//        return "1";
//    }
}
