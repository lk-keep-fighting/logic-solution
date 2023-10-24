package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.enums.ConceptEnum;

public class VariableTreeNode extends ParamTreeNode {
    public VariableTreeNode(String name){
        super(name);
        concept= ConceptEnum.Variable;
    }
}
