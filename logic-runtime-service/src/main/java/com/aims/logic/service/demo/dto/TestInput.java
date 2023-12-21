package com.aims.logic.service.demo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TestInput {
    public String str;
    int i;
    List<String> arr;
}
