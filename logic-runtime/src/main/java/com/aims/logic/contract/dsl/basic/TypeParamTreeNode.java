package com.aims.logic.contract.dsl.basic;

import com.aims.logic.contract.enums.ConceptEnum;

public class TypeParamTreeNode {
    public TypeParamTreeNode(String name){
        this.name=name;
        this.concept= ConceptEnum.TypeParam;
    }
    ConceptEnum concept; // 产品概念
    String name; // 类型名称
}
