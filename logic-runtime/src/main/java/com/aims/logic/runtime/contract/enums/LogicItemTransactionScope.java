package com.aims.logic.runtime.contract.enums;

import lombok.Getter;

@Getter
public enum LogicItemTransactionScope {
    /**
     * 默认，缺省配置为tranGroup
     */
    def("def"),
    /**
     * 关闭事务
     */
    off("off"),
    /**
     * 按事务组提交数据
     */
    tranGroup("tranGroup"),
    everyRequest("everyRequest");
//    @Deprecated
//    everyJavaNode("everyJavaNode"),
//    /**
//     * 按节点提交数据，发生异常时，为配置的业务异常类则不中断，否则中断
//     */
//    @Deprecated
//    everyNode2("everyNode2"),
//    /**
//     * 按节点提交数据，发生异常则中断
//     */
//    @Deprecated
//    everyNode("everyNode");
    final String value;

    LogicItemTransactionScope(String value) {
        this.value = value;
    }

}
