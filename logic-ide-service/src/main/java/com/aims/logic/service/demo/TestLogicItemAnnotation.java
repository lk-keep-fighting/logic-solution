package com.aims.logic.service.demo;

import com.aims.logic.sdk.annotation.LogicItem;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.List;

@Component
public class TestLogicItemAnnotation {
    @LogicItem(name = "测试字符串参数", group = "公共组件", order = "2")
    public String testStringArgs(String input) {
        return input;
    }

    @LogicItem(name = "测试List泛型参数", group = "公共组件", order = "1")
    public List<String> testListArgs(List<String> input) {
        return input;
    }

    @LogicItem(name = "测试数值参数", group = "业务组件1")
    public int testIntArgs(int input) {
        return input;
    }


    @LogicItem(name = "测试数组参数", group = "业务组件1")
    public Array testArrayArgs(Array input) {
        return input;
    }


    @LogicItem(name = "测试数值参数", group = "业务组件2")
    public BigDecimal testBigDecimalArgs(BigDecimal input) {
        return input;
    }
}
