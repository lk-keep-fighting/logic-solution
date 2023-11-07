package com.aims.logic.contract.dsl;

import com.aims.logic.contract.enums.ConceptEnum;

public class VariableTreeNode extends ParamTreeNode {
    public VariableTreeNode(String name){
        super(name);
        concept= ConceptEnum.Variable;
    }
}
