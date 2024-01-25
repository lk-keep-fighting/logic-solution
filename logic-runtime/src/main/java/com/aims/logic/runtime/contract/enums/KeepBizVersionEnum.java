package com.aims.logic.runtime.contract.enums;

public enum KeepBizVersionEnum {
    on("on"),
    off("off");

    private final String value;

    KeepBizVersionEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
