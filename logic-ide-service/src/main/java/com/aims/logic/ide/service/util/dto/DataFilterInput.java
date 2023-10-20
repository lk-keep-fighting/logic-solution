package com.aims.logic.ide.service.util.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataFilterInput {
    private  String dataIndex;
    private List<String> values;
    private String type;
}
