package com.aims.logic.ide.controller.dto;

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
