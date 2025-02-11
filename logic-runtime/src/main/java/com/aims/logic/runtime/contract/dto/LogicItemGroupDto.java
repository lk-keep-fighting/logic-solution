package com.aims.logic.runtime.contract.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LogicItemGroupDto {
    String name = "";
    String shape = "";
    String order = "";
}
