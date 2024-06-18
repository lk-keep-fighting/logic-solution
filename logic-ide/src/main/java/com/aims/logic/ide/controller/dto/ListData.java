package com.aims.logic.ide.controller.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ListData {
    List<Map<String, Object>> items;
    public ListData(List<Map<String, Object>> items) {
        this.items = items;
    }
}
