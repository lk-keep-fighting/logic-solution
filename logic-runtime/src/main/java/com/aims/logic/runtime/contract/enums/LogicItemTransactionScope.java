package com.aims.logic.runtime.contract.enums;

public enum LogicItemTransactionScope {
    off("off"),
    on("on"),
    everyJavaNode("everyJavaNode"),
    everyRequest("everyRequest");
    final String value;

    LogicItemTransactionScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
