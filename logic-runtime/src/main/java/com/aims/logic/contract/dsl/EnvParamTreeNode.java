package com.aims.logic.contract.dsl;

import com.aims.logic.contract.enums.ConceptEnum;

public class EnvParamTreeNode extends ParamTreeNode {
    public EnvParamTreeNode(String name) {
        super(name);
        concept = ConceptEnum.EnvParam;
    }
}
