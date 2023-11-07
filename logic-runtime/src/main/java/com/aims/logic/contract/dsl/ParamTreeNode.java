package com.aims.logic.contract.dsl;

import com.aims.logic.contract.enums.ConceptEnum;
import com.aims.logic.contract.dsl.basic.BaseLASL;
import com.aims.logic.contract.dsl.basic.TypeAnnotationTreeNode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liukun
 */
@Getter
@Setter
public class ParamTreeNode extends BaseLASL {
    public ParamTreeNode(String name) {
        this.concept = ConceptEnum.Param;
    }

    private String name;
    private String describe;
    private TypeAnnotationTreeNode typeAnnotation;
    private boolean required;
    private String defaultValue;
}
