package com.aims.logic.runtime.contract.enums;

public enum LogicItemType {
    start("start"),
    waitForContinue("wait-for-continue"),
    switchNode("switch"),
    switchCaseNode("switch-case"),
    switchDefaultNode("switch-default"),
    java("java");

    final String value;

    LogicItemType(String _value) {
        this.value = _value;
    }

    public String getValue() {
        return this.value;
    }
    public boolean compareType(String _value) {
        return this.value.equals(_value);
    }
}
