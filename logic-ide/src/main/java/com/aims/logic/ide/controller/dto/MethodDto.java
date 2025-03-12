package com.aims.logic.ide.controller.dto;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class MethodDto {
    public void Method() {

    }

    public MethodDto(Method method, MethodSourceCodeDto sourceCodeDto) {
        this.method = method;
        this.sourceCodeDto = sourceCodeDto;
    }

    Method method;
    MethodSourceCodeDto sourceCodeDto;
}
