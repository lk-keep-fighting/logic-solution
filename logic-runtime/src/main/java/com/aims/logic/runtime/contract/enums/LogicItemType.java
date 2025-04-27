package com.aims.logic.runtime.contract.enums;

import lombok.Getter;

@Getter
public enum LogicItemType {
    start("start"),
    wait("wait"),
    waitForContinue("wait-for-continue"),
    switchNode("switch"),
    switchCaseNode("switch-case"),
    switchDefaultNode("switch-default"),
    java("java");


    final String value;

    LogicItemType(String _value) {
        this.value = _value;
    }

    public boolean equalsTo(String _value) {
        return this.value.equals(_value);
    }
}
