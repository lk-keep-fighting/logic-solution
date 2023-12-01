package com.aims.logic.contract.dsl;

import com.aims.logic.contract.enums.ConceptEnum;
import com.aims.logic.contract.dsl.basic.BaseLASL;
import com.alibaba.fastjson2.JSONObject;
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
    String code;
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
