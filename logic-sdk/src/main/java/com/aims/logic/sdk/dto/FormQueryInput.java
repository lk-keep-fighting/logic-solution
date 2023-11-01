package com.aims.logic.sdk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FormQueryInput {
    private List<String> ids;
    private List<DataFilterInput> filters;
    private List<OrderByInput> orderBy;
    private int page = 1;
    private int pageSize = 10;
}
