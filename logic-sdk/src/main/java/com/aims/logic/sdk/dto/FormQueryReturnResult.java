package com.aims.logic.sdk.dto;

import lombok.Data;

import java.util.List;
@Data
public class FormQueryReturnResult {
    private List<Object> items;
    private int totalCount;
}
