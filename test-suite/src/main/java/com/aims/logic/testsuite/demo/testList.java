package com.aims.logic.testsuite.demo;


import com.aims.logic.sdk.annotation.LogicItem;
import com.aims.logic.testsuite.demo.dto.ListType;
import com.aims.logic.testsuite.demo.entity.TestEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class testList {
    @LogicItem(name = "测试list", group = "测试复杂类型")
    public List<ListType> listTypeList(List<ListType> list) {
        return list;
    }

    @LogicItem(name = "测试字符串数组", group = "测试数组")
    public List<String> listString(List<String> list) {
        return list;
    }

    @LogicItem(name = "测试实体数组", group = "测试数组")
    public List<TestEntity> listEntity(List<TestEntity> list) {
        return list;
    }

    public Map<String, ListType> mapList(Map<String, ListType> map) {
        return map;
    }
}
