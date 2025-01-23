package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.enums.ConceptEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ReturnTreeNode extends ParamTreeNode {
    public ReturnTreeNode(String name) {
        super(name);
        this.concept = ConceptEnum.Return;
    }
}
