package com.aims.logic.runtime.contract.enums;

public enum LogicItemTransactionScope {
    off("off"),
//    on("on"),
    def("def"),
    everyJavaNode("everyJavaNode"),
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
