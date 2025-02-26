package com.aims.logic.runtime.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessedParameter {
    private Class<?> parameterType;
    private Object parameterValue;
}