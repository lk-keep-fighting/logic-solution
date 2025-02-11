package com.aims.logic.testsuite.demo;

import com.aims.logic.sdk.annotation.LogicItem;
import org.springframework.stereotype.Component;

@Component
public class TestLogicItemAnnotation {
    @LogicItem(name = "测试字符串参数", type = "java")
    public String testStringArgs(String input) {
        return input;
    }
}
