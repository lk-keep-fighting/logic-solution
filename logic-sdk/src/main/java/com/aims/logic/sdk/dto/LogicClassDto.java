package com.aims.logic.sdk.dto;

import lombok.Data;

@Data
public class LogicClassDto {
    public LogicClassDto() {
    }

    public LogicClassDto(String value) {
        this.value = value;
    }

    String value;
}
