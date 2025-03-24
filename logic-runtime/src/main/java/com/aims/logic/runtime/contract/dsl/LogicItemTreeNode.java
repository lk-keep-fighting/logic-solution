package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.enums.ConceptEnum;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class LogicItemTreeNode extends BaseLASL {
    public LogicItemTreeNode() {
        concept = ConceptEnum.LogicItem;
    }

    String id;
    String name;
    /**
     * 节点代码
     */
    String code;
    String type;
    String script;
    /**
     * 业务标识，可以是js表达式
     */
    String bizId;
    String url;
    /**
     * 方法格式为方法名(参数名称1，参数2名称2)
     */
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
    /**
     * java等强类型返回值声明
     */
    ReturnTreeNode returnType;
    String timeout;
    String nextId;

    // 设置方法的值，方法格式为方法名(参数名称1，参数2名称2)
    public void setMethod(String methodName, String[] paramNames) {
        this.method = methodName + "(" + StringUtils.join(paramNames, ",") + ")";
    }

    public String getTimeout() {
        return StringUtils.isBlank(timeout) ? "5000" : timeout;
    }

    /**
     * 事务范围
     * EveryJavaNode-每个java节点开启事务
     * EveryJavaNode2-每个java节点开启事务但报错不会中断
     * EveryRequest-每次请求交互开启事务，即每个交互点
     * off-关闭事务
     */
    LogicItemTransactionScope tranScope;
    /*
    事务组id
     */
    String tranGroupId;
    /*
    源代码
     */
    String sourceCode;
    /*
    git信息
     */
    String gitInfo;
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
    /*
    是否异步执行，异步执行将不等待返回结果，默认成功
     */
    boolean async = false;
    String memo;
    /*
    节点执行时实例化的对象唯一编号
    用于循环调用时的链路追踪
     */
    String objectId;
}
