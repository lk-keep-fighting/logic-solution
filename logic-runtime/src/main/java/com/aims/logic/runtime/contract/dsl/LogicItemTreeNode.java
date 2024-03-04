package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.enums.ConceptEnum;
import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
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
    /**
     * http请求body
     */
    String body;
    /**
     * java等强类型参数声明
     */
    List<ParamTreeNode> params;
    String timeout;
    String nextId;

    public String getTimeout() {
        return timeout == null ? "5000" : timeout;
    }

    /**
     * 事务范围
     * EveryJavaNode-每个java节点开启事务
     * EveryRequest-每次请求交互开启事务，即每个交互点
     * off-关闭事务
     */
    LogicItemTransactionScope tranScope;
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
