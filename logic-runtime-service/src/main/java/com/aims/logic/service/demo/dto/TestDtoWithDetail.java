package com.aims.logic.service.demo.dto;

import com.aims.logic.service.demo.entity.TestDetailEntity;
import lombok.Data;

import java.util.List;

@Data
public class TestDtoWithDetail {
    String id;
    String name;
    List<TestDetailEntity> detailEntityList;
}
