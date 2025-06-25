package com.aims.logic.runtime.contract.enums;

import lombok.Getter;

@Getter
public enum LogicItemTransactionScope {
    off("off"),
//    on("on"),
    def("def"),
    @Deprecated
    everyJavaNode("everyJavaNode"),
    /**
     * 按节点提交数据，发生异常时，为配置的业务异常类则不中断，否则中断
     */
    everyNode2("everyNode2"),
    /**
     * 按节点提交数据，发生异常则中断
     */
    everyNode("everyNode"),
    everyRequest("everyRequest");
    final String value;

    LogicItemTransactionScope(String value) {
        this.value = value;
    }

}
