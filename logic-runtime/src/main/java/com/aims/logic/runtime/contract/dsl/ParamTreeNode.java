package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.enums.ConceptEnum;
import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.dsl.basic.TypeAnnotationTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author liukun
 */
@Getter
@Setter
@Accessors(chain = true)
public class ParamTreeNode extends BaseLASL {
    public ParamTreeNode(String name) {
        this.concept = ConceptEnum.Param;
        this.name = name;
    }

    private String name;
    //    private String describe;
    private String className;
    private TypeAnnotationTreeNode typeAnnotation;
    private boolean required;
    private String defaultValue;

    @Override
    public boolean equals(Object obj) {
        ParamTreeNode paramTreeNode = (ParamTreeNode) obj;
        return paramTreeNode != null && paramTreeNode.getName().equals(this.name) && paramTreeNode.typeAnnotation != null && paramTreeNode.typeAnnotation.getTypeName().equals(this.typeAnnotation.getTypeName());
    }
}
