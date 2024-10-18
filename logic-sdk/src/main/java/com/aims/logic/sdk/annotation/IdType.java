package com.aims.logic.sdk.annotation;

public enum IdType {
    NOT_SET("NOT_SET"),
    ASSIGN_ID("ASSIGN_ID"),
    UUID("UUID");

    private String type;

    IdType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
