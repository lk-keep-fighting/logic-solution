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
    public String getCbbId() {
        return logicItem.getCbbId();
    }

    public String getItemId() {
        return logicItem.getItemId();
    }

    public String getType() {
        return logicItem.getType();
    }

    public String getName() {
        return logicItem.getName();
    }

    public String getGroup() {
        return logicItem.getGroup();
    }

    public String getVersion() {
        return logicItem.getVersion();
    }

    String shape = "";
    String order = "";
    MethodSourceCodeDto codeInfo;
    LogicItemTreeNode logicItem;
    List<ParamTreeNode> parameters = new ArrayList<>();
    ReturnTreeNode returnType;

}
