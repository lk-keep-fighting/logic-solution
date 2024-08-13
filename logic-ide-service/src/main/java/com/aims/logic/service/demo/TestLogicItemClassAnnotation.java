package com.aims.logic.service.demo;

import com.aims.logic.sdk.annotation.LogicItemJavaMethodInClass;
import org.springframework.stereotype.Component;

@Component
@LogicItemJavaMethodInClass
public class TestLogicItemClassAnnotation {
    public String testStringArgs(String input) {
        return input;
    }
}
