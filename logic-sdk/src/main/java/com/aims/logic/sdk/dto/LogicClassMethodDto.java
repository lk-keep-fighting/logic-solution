package com.aims.logic.sdk.dto;

import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class LogicClassMethodDto {
    String name;
    List<ParamTreeNode> parameters = new ArrayList<>();
//    List<TypeAnnotationTreeNode> parameters = new ArrayList<>();
}
