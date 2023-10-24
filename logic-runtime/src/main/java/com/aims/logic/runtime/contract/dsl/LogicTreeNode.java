package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.dsl.basic.TypeParamTreeNode;
import com.aims.logic.runtime.contract.enums.ConceptEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
    逻辑描述
 */
@Getter
@Setter
public class LogicTreeNode extends BaseLASL {

    public LogicTreeNode() {
        concept= ConceptEnum.Logic;
    }

    String name;
    /*
    schema格式版本号
     */

    String schemaVersion;
    /*
    配置版本号
     */
    String version;
    String label;

    String description;

    String triggerType;

    String cron;

    List<TypeParamTreeNode> typeParams;

    List<ParamTreeNode> params;

    List<ReturnTreeNode> returns;

    List<VariableTreeNode> variables;

    List<EnvParamTreeNode> envs;

    List<LogicItemTreeNode> items;

    Object visualConfig;

}