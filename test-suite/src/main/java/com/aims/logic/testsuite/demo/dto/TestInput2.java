package com.aims.logic.testsuite.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestInput2 {
    public List<TestInput> inputs;
    public String[] strs;
}
