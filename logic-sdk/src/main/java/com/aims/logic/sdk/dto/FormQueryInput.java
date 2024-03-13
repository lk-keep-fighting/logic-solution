package com.aims.logic.sdk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FormQueryInput {
    private List<String> ids;
    /**
     * 条件直接默认是或，指定type为=时是and查询
     */
    private List<DataFilterInput> filters;
    private List<OrderByInput> orderBy;
    private int page = 1;
    private int pageSize = 10;
}
