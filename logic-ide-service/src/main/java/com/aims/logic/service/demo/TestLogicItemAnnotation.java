package com.aims.logic.service.demo;

import com.aims.logic.sdk.annotation.LogicItemJavaMethod;
import org.springframework.stereotype.Component;

@Component
public class TestLogicItemAnnotation {
    @LogicItemJavaMethod(name = "测试字符串参数", type = "java")
    public String testStringArgs(String input) {
        return input;
    }
}
