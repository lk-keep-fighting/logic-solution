package com.aims.logic.testsuite.demo.component;

import com.aims.logic.sdk.annotation.LogicItem;
import com.aims.logic.testsuite.demo.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
public class ExceptionHandler {
    @LogicItem(name = "抛出异常", group = "异常处理")
    public void throwException(String msg) {
        throw new CustomException(msg);
    }
}
