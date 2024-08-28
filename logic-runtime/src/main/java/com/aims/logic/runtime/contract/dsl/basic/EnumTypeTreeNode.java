package com.aims.logic.runtime.contract.dsl.basic;

import lombok.Data;

import java.util.List;

@Data
public class EnumTypeTreeNode extends BaseLASL {
    private String name;
    private List<EnumTypeItem> enumItems;
    private TypeAnnotationTreeNode valueType;
}
