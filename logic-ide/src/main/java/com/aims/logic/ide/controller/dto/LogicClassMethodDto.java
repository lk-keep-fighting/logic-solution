package com.aims.logic.ide.controller.dto;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.contract.dsl.ReturnTreeNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class LogicClassMethodDto {
    String name;
    String type = "java";
    String group = "预声明业务方法";
    String shape = "";
    String order = "";
    MethodSourceCodeDto codeInfo;
    LogicItemTreeNode logicItem;
    List<ParamTreeNode> parameters = new ArrayList<>();
    ReturnTreeNode returnType;
}
