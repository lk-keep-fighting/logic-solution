package com.aims.logic.sdk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DataFilterInput {
    private String dataIndex;
    private List<String> values;
    private String type;
}
