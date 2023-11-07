package com.aims.logic.contract.enums;

public enum EnvEnum {
    dev("dev"),
    test("test"),
    demo("demo"),
    pre("pre"),
    prod("prod");
    private final String value;
    EnvEnum(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
