package com.aims.logic.runtime.contract.enums;

public enum LogicItemTransactionScope {
    off("off"),
//    on("on"),
    def("def"),
    @Deprecated
    everyJavaNode("everyJavaNode"),
    everyNode2("everyNode2"),
    everyNode("everyNode"),
    everyRequest("everyRequest");
    final String value;

    LogicItemTransactionScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
