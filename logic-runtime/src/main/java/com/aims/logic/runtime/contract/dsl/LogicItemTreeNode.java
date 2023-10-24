package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.enums.ConceptEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogicItemTreeNode extends BaseLASL {
    public LogicItemTreeNode() {
        concept = ConceptEnum.LogicItem;
    }

    String id;
    String name;
    String type;
    String script;
    String url;
    String method;
    String headers;
    String body;
    String timeout;
    String nextId;
    /*
    条件分支表达式
     */
    String condition;
    /*
    返回值接收参数
     */
    String returnAccept;
    /*
    switch分支
     */
    List<LogicItemBranch> branches;
}
