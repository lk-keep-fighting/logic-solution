package com.aims.logic.contract.dsl;

import com.aims.logic.contract.enums.ConceptEnum;

public class ReturnTreeNode extends ParamTreeNode {
    public ReturnTreeNode(String name) {
        super(name);
        this.concept = ConceptEnum.Return;
    }
}
