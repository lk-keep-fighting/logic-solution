package com.aims.logic.testsuite.demo.dto;

public enum EnumType {
    A("A"),
    B("B"),
    C("C");

    private String value;

    EnumType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
