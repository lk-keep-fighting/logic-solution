package com.aims.logic.sdk.dto;

import com.aims.logic.contract.dsl.ParamTreeNode;
import com.aims.logic.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.contract.dsl.basic.TypeParamTreeNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class LogicClassMethodDto {
    String name;
    List<ParamTreeNode> parameters = new ArrayList<>();
//    List<TypeAnnotationTreeNode> parameters = new ArrayList<>();
}
