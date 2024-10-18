package com.aims.logic.testsuite.demo.dto;

public class ParametricType<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
