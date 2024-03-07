package com.aims.logic.service.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestInput2 {
    public List<TestInput> inputs;
    public String[] strs;
}
