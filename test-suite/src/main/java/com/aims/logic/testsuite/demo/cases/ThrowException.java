package com.aims.logic.testsuite.demo.cases;

import com.aims.logic.testsuite.demo.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
public class ThrowException {

    public void throwCustomException(String msg) {
        throw new CustomException("抛出自定义异常" + msg);
    }
}
