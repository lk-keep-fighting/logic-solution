package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.enums.ConceptEnum;

public class ReturnTreeNode extends ParamTreeNode {
    public ReturnTreeNode(String name) {
        super(name);
        this.concept = ConceptEnum.Return;
    }
}
