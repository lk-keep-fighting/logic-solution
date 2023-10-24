package com.aims.logic.runtime.contract.enums;

public enum LogicConfigModelEnum {
    online("online"),
    offline("offline");
    private final String value;
    LogicConfigModelEnum(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
