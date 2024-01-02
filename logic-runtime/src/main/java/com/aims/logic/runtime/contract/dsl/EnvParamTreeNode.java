package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.enums.ConceptEnum;

public class EnvParamTreeNode extends ParamTreeNode {
    public EnvParamTreeNode(String name) {
        super(name);
        concept = ConceptEnum.EnvParam;
    }
}
