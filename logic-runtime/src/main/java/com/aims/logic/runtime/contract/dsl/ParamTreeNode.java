package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.runtime.contract.enums.ConceptEnum;
import lombok.Data;

//import org.json.JSONObject;
@Data
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
